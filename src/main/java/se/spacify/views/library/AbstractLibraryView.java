package se.spacify.views.library;

import se.spacify.controls.GroupedListPanel;
import se.spacify.controls.Table;
import se.spacify.controls.ToggleButton;
import se.spacify.controls.ToolBar;
import se.spacify.controls.ToolButton;
import se.spacify.graphics.GroupAvatar;
import se.spacify.library.LibraryEvents;
import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;
import se.spacify.service.media.PlaybackCoordinator;
import se.spacify.service.media.PlayQueue;
import se.spacify.service.media.PlayQueueItem;
import se.spacify.service.media.PlayRequest;

import se.spacify.ui.theme.ThemeManager;
import se.spacify.ui.theme.ThemedTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base for the data-backed library views. Provides a themed {@link JTable} with
 * an optional header and a CRUD toolbar (Add / Edit / Delete / Scan / Refresh).
 * Read-only views (see {@link #isEditable()}) keep just Refresh; mutating
 * actions broadcast via {@link LibraryEvents} so the sidebar stays in sync.
 */
public abstract class AbstractLibraryView extends View {

	protected final JPanel panel;
	protected final JLabel headerLabel;
	protected final Table table;
	protected final JScrollPane scroll;
	protected final DefaultTableModel model;
	private ToolBar bottomToolbar;
	private ToolBar toolbar;
	private ToolButton refreshBtn;

	// ── Optional grouped presentation (see supportsGrouping/groupings) ──────────
	private GroupedListPanel groupedPanel;
	private ToggleButton groupToggle;
	private JComboBox<Grouping> groupingChooser;
	private boolean grouped;

	/**
	 * Identity and display text of the group a row belongs to. {@code key} is the
	 * stable group identity (drives the placeholder image colour and de-dupes
	 * rows into sections); {@code title}/{@code subtitle} are shown in the section
	 * header — e.g. an album name and its artists.
	 */
	public record GroupRef(String key, String title, String subtitle) {}

	/** A named way of grouping the current rows (e.g. "Release", "Playlist"). */
	public interface Grouping {
		String name();

		/** The group the given model row belongs to. */
		GroupRef groupOf(int modelRow);
	}

	protected AbstractLibraryView(ViewStack viewStack) {
		super(viewStack);
		panel = new JPanel(new BorderLayout(0, 8));
		panel.setOpaque(false);

		headerLabel = new JLabel();
		headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));
		headerLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		headerLabel.setVisible(false);

		// ── Table ────────────────────────────────────────────────────────────────
		model = new DefaultTableModel(getColumns(), 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		table = new Table(model);
		table.setFillsViewportHeight(true);
		table.setRowHeight(28);
		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				if (row < 0)
					return;
				if (e.getClickCount() == 2) {
					activate(row);
				} else if (e.getClickCount() == 1) {
					onCellClicked(row, table.columnAtPoint(e.getPoint()));
				}
			}

			@Override public void mousePressed(MouseEvent e)  { maybeShowRowMenu(e); }
			@Override public void mouseReleased(MouseEvent e) { maybeShowRowMenu(e); }
		});

		ThemedTableCellRenderer renderer = new ThemedTableCellRenderer();
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		scroll = new JScrollPane(table);
		// Non-UIResource empty border so the Nimbus reinstall on theme change
		// doesn't re-install a default scroll-pane border.
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(true);
		scroll.getViewport().setOpaque(true);

		// ── CRUD toolbar ───────────────────────────────────────────────────────
		toolbar = new ToolBar();

		refreshBtn = new ToolButton("Refresh");
		refreshBtn.addActionListener(e -> reloadAndRegroup());

		if (isEditable()) {
			ToolButton addBtn = new ToolButton("Add");
			ToolButton editBtn = new ToolButton("Edit");
			ToolButton deleteBtn = new ToolButton("Delete");
			ToolButton scanBtn = new ToolButton("Scan…");

			scanBtn.addActionListener(e -> LibraryScanAction.run(panel, () -> {
				reloadAndRegroup();
				LibraryEvents.fireChanged();
			}));
			addBtn.addActionListener(e -> {
				onAdd();
				reloadAndRegroup();
				LibraryEvents.fireChanged();
			});
			editBtn.addActionListener(e -> {
				int row = table.getSelectedRow();
				if (row >= 0) {
					onEdit(row);
					reloadAndRegroup();
					LibraryEvents.fireChanged();
				}
			});
			deleteBtn.addActionListener(e -> {
				int row = table.getSelectedRow();
				if (row >= 0) {
					onDelete(row);
					reloadAndRegroup();
					LibraryEvents.fireChanged();
				}
			});

			toolbar.add(addBtn);
			toolbar.add(editBtn);
			toolbar.add(deleteBtn);
			toolbar.addSeparator();
			toolbar.add(scanBtn);
		}
		toolbar.add(refreshBtn);

		// ── Optional grouped-view switch ────────────────────────────────────────
		// supportsGrouping() must be a constant (it's called mid-construction);
		// the actual Grouping list is read lazily once the subclass is built.
		if (supportsGrouping()) {
			groupedPanel = new GroupedListPanel();
			groupingChooser = new JComboBox<>();
			groupingChooser.setRenderer(new DefaultListCellRenderer() {
				private static final long serialVersionUID = 1L;
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean hasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
					setText(value instanceof Grouping g ? g.name() : "");
					return this;
				}
			});
			groupingChooser.setVisible(false);
			groupingChooser.addActionListener(e -> { if (grouped) rebuildGroups(); });

			groupToggle = new ToggleButton("Grouped");
			groupToggle.addActionListener(e -> setGrouped(groupToggle.isSelected()));

			toolbar.addSeparator();
			toolbar.add(groupToggle);
			toolbar.add(groupingChooser);
		}

		// Subclass-contributed toolbar control (e.g. a catalogue search field).
		JComponent accessory = toolbarAccessory();
		if (accessory != null) {
			toolbar.addSeparator();
			toolbar.add(accessory);
		}

		JPanel north = new JPanel(new BorderLayout());
		north.setOpaque(false);
		north.add(toolbar, BorderLayout.CENTER);
		panel.add(headerLabel, BorderLayout.NORTH);

		panel.add(north, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);

		bottomToolbar = new ToolBar();
		bottomToolbar.add(new JButton("Test"));
		panel.add(bottomToolbar, BorderLayout.SOUTH);

		updateColors();
		ThemeManager.addChangeListener(this::updateColors);
		// Repaint so the now-playing row highlight follows the active track.
		PlayQueue.getInstance().addChangeListener(table::repaint);
	}

	/** Sets the page header text; pass null/blank to hide it. */
	protected void setHeader(String text) {
		headerLabel.setText(text == null ? "" : text);
		headerLabel.setVisible(text != null && !text.isBlank());
	}

	/**
	 * Whether this view shows Add/Edit/Delete/Scan; read-only views return false.
	 */
	protected boolean isEditable() {
		return true;
	}

	/**
	 * Whether this view offers the optional grouped presentation (a "Grouped"
	 * toggle plus a grouping chooser). Must be constant — it is queried during
	 * construction. Views that return true must also override {@link #groupings()}
	 * and produce play-queue items via {@link #queueItemAt(int)}.
	 */
	protected boolean supportsGrouping() {
		return false;
	}

	/**
	 * The grouping options offered when {@link #supportsGrouping()} is true; read
	 * lazily after construction. The first entry is the default selection.
	 */
	protected List<Grouping> groupings() {
		return List.of();
	}

	// ── Hooks for subclasses ────────────────────────────────────────────────────

	/** Column headers; called once during construction (must be constant). */
	protected abstract String[] getColumns();

	/** Clear and refill {@link #model} from the database. */
	protected abstract void reload();

	protected void onAdd() {
	}

	protected void onEdit(int row) {
	}

	protected void onDelete(int row) {
	}

	/** Invoked when a row is double-clicked; default does nothing. */
	protected void onActivate(int row) {
	}

	/** Invoked on a single click of a cell; default does nothing. Used e.g. by the
	 *  catalogue recordings view to toggle a row's "in library" (＋/✓) column. */
	protected void onCellClicked(int row, int col) {
	}

	/**
	 * An optional component contributed to the right of the toolbar — e.g. a
	 * catalogue search field. Called once during construction; returning a freshly
	 * built component (not a re-initialised field) keeps the reference stable.
	 */
	protected JComponent toolbarAccessory() {
		return null;
	}

	/**
	 * Supply a {@link PlayRequest} for the given model row, or {@code null} if the
	 * row isn't playable. This is the preferred hook for metadata-resolved views:
	 * the default {@link #queueItemAt(int)} builds a queue entry from it whose
	 * playback runs through {@link PlaybackCoordinator#resolveAndPlay}, and the
	 * right-click "Play with…" menu uses it too. Views that play something concrete
	 * (a local file) may instead override {@link #queueItemAt(int)} directly.
	 */
	protected PlayRequest playRequestAt(int row) {
		return null;
	}

	/**
	 * Supply a {@link PlayQueueItem} for the given model row, or {@code null} if
	 * the row isn't playable. By default this derives from {@link #playRequestAt}
	 * (routing playback through the "Play with…" resolver); views that play a
	 * concrete target may override it. Views that produce neither fall back to
	 * {@link #onActivate(int)}.
	 */
	protected PlayQueueItem queueItemAt(int row) {
		PlayRequest req = playRequestAt(row);
		if (req == null)
			return null;
		return new PlayQueueItem(req.key(), req.title(), req.artist(), req.durationMs(),
				() -> PlaybackCoordinator.resolveAndPlay(req), req);
	}

	/**
	 * Pop the row context menu ("Play with…") on a platform popup trigger, when the
	 * row under the cursor is playable.
	 */
	private void maybeShowRowMenu(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;
		int row = table.rowAtPoint(e.getPoint());
		if (row < 0)
			return;
		table.setRowSelectionInterval(row, row);
		PlayRequest req = playRequestAt(row);
		if (req == null)
			return;
		JPopupMenu menu = new JPopupMenu();
		JMenuItem playWith = new JMenuItem("Play with…");
		playWith.addActionListener(a -> PlaybackCoordinator.resolveAndPlay(req, true));
		menu.add(playWith);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Turn the visible rows into the play queue and start at {@code row}. Every
	 * playable row (per {@link #queueItemAt}) becomes a queue entry; if the view
	 * doesn't produce queue items, falls back to {@link #onActivate(int)}.
	 */
	private void activate(int row) {
		List<PlayQueueItem> queue = new ArrayList<>();
		int startIndex = -1;
		for (int i = 0; i < model.getRowCount(); i++) {
			PlayQueueItem item = queueItemAt(i);
			if (item == null)
				continue;
			if (i == row)
				startIndex = queue.size();
			queue.add(item);
		}
		if (startIndex >= 0) {
			PlayQueue.getInstance().setQueueAndPlay(queue, startIndex);
		} else {
			onActivate(row);
		}
	}

	// ── Grouped presentation ────────────────────────────────────────────────────

	/** Repopulate the model, then refresh the grouped view if it's showing. */
	protected void reloadAndRegroup() {
		reload();
		if (grouped)
			rebuildGroups();
	}

	private void setGrouped(boolean on) {
		grouped = on;
		groupingChooser.setVisible(on);
		if (on) {
			if (groupingChooser.getItemCount() == 0)
				for (Grouping g : groupings())
					groupingChooser.addItem(g);
			rebuildGroups();
			scroll.setViewportView(groupedPanel);
		} else {
			scroll.setViewportView(table);
		}
		scroll.revalidate();
		scroll.repaint();
	}

	/** Rebuild the grouped sections from the current model rows. */
	private void rebuildGroups() {
		if (groupedPanel == null)
			return;
		Grouping g = (Grouping) groupingChooser.getSelectedItem();
		if (g == null) {
			groupedPanel.setGroups(List.of());
			return;
		}
		// Preserve model row order within each section (rows already arrive in
		// album order), and section order by first appearance.
		Map<String, GroupBucket> buckets = new LinkedHashMap<>();
		for (int row = 0; row < model.getRowCount(); row++) {
			PlayQueueItem item = queueItemAt(row);
			if (item == null)
				continue;
			GroupRef maybe = g.groupOf(row);
			final GroupRef ref = maybe != null ? maybe : new GroupRef("none", "Unknown", "");
			GroupBucket bucket = buckets.computeIfAbsent(ref.key(), k -> new GroupBucket(ref));
			final int r = row;
			bucket.items.add(new GroupedListPanel.Item(
					item.getName(), item.getArtists(), fmtDuration(item.getDurationMs()),
					item.getKey(), () -> activate(r)));
		}
		List<GroupedListPanel.Group> groups = new ArrayList<>();
		for (GroupBucket bucket : buckets.values()) {
			Image image = GroupAvatar.of(bucket.ref.key(), bucket.ref.title(), 64);
			groups.add(new GroupedListPanel.Group(
					image, bucket.ref.title(), bucket.ref.subtitle(), bucket.items));
		}
		groupedPanel.setGroups(groups);
	}

	private static final class GroupBucket {
		final GroupRef ref;
		final List<GroupedListPanel.Item> items = new ArrayList<>();

		GroupBucket(GroupRef ref) {
			this.ref = ref;
		}
	}

	// ── Shared helpers ──────────────────────────────────────────────────────────

	protected static String fmtDuration(long ms) {
		if (ms <= 0)
			return "";
		long s = ms / 1000;
		return String.format("%d:%02d", s / 60, s % 60);
	}

	/** Parse "m:ss" or a plain seconds value into milliseconds; 0 on failure. */
	protected static long parseDuration(String text) {
		if (text == null)
			return 0;
		String t = text.trim();
		if (t.isEmpty())
			return 0;
		try {
			if (t.contains(":")) {
				String[] p = t.split(":");
				return (Long.parseLong(p[0].trim()) * 60 + Long.parseLong(p[1].trim())) * 1000;
			}
			return (long) (Double.parseDouble(t) * 1000);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	protected void showError(Exception e) {
		JOptionPane.showMessageDialog(panel, e.getMessage(), "Library error", JOptionPane.ERROR_MESSAGE);
	}

	protected boolean confirmDelete(String what) {
		return JOptionPane.showConfirmDialog(panel, "Delete " + what + "?", "Confirm delete", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}

	private void updateColors() {
		Color bg = ThemeManager.getBackground();
		Color fg = ThemeManager.getForeground();
		Color grid = ThemeManager.getGridColor();

		headerLabel.setForeground(fg);
		table.setBackground(bg);
		table.setForeground(fg);
		table.setGridColor(grid);
		scroll.setBackground(bg);
		scroll.getViewport().setBackground(bg);
		table.repaint();
	}

	// ── SPView ──────────────────────────────────────────────────────────────────

	@Override
	public void navigate(String uri) {
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void onShow() {
		reloadAndRegroup();
	}
}
