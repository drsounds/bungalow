package se.spacify.app.music.views;

import se.spacify.controls.GroupedListPanel;
import se.spacify.controls.Table;
import se.spacify.graphics.GroupAvatar;
import se.spacify.navigation.ViewStack;
import se.spacify.service.media.AvailabilityResolver;
import se.spacify.service.media.PlaybackCoordinator;
import se.spacify.service.media.PlayQueue;
import se.spacify.service.media.PlayQueueItem;
import se.spacify.service.media.PlayRequest;
import se.spacify.service.media.TrackAvailability;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.ui.theme.ThemedTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A reusable, theme-aware music list table that any Spacify app can embed without
 * subclassing a view. It presents a {@link Table} whose rows are playable items
 * (tracks, downloads, catalogue results) and adds the music-specific chrome that
 * should look and behave the same everywhere:
 * <ul>
 *   <li>a trailing Buy/Stream split-button column driven by per-row
 *       {@link TrackAvailability} (or just a file glyph when matched locally);</li>
 *   <li>an optional library-membership (✓/＋) column;</li>
 *   <li>double-click to turn the visible rows into the play queue;</li>
 *   <li>a right-click "Play with…" menu; and</li>
 *   <li>an optional grouped (album/playlist) presentation.</li>
 * </ul>
 * The embedding code fills the {@linkplain #getModel() model} and supplies per-row
 * playback metadata and callbacks through a {@link Source}. Call {@link #refresh()}
 * after (re)populating the model.
 */
public class MusicTable extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Width of the Buy/Stream column, and the ▾ (menu) hit zone within it. */
	private static final int BUY_STREAM_COL_WIDTH = 94;
	private static final int BUY_STREAM_ARROW_W   = 18;
	private static final int LIB_TOGGLE_COL_WIDTH = 32;

	/**
	 * Per-row playback metadata and interaction callbacks supplied by the embedding
	 * app. Only {@link #playRequestAt(int)} is required; the rest have sensible
	 * defaults, so a read-only list need implement just that one method.
	 */
	public interface Source {

		/** A {@link PlayRequest} for the model row, or {@code null} if it isn't playable. */
		PlayRequest playRequestAt(int row);

		/**
		 * A {@link PlayQueueItem} for the model row, or {@code null}. By default this
		 * derives from {@link #playRequestAt} (routing playback through the
		 * "Play with…" resolver); views that play a concrete target may override it.
		 */
		default PlayQueueItem queueItemAt(int row) {
			PlayRequest req = playRequestAt(row);
			if (req == null)
				return null;
			return new PlayQueueItem(req.key(), req.title(), req.artist(), req.durationMs(),
					() -> PlaybackCoordinator.resolveAndPlay(req), req);
		}

		/** Whether the row's track is saved in the local library (drives ✓ vs ＋). */
		default boolean inLibraryAt(int row) { return false; }

		/** Add/remove the row's track to/from the local library. */
		default void toggleLibraryAt(int row) {}

		/** Fallback when the view produces no play-queue items (e.g. open a detail). */
		default void onActivate(int row) {}

		/** Single-click on a cell that isn't one of the managed columns. */
		default void onCellClicked(int row, int col) {}

		/** Grouping options for the grouped presentation; read lazily, may be empty. */
		default List<Grouping> groupings() { return List.of(); }
	}

	/**
	 * Identity and display text of the group a row belongs to. {@code key} is the
	 * stable group identity (drives the placeholder image colour and de-dupes rows
	 * into sections); {@code title}/{@code subtitle} are shown in the section header.
	 */
	public record GroupRef(String key, String title, String subtitle) {}

	/** A named way of grouping the current rows (e.g. "Release", "Playlist"). */
	public interface Grouping {
		String name();

		/** The group the given model row belongs to. */
		GroupRef groupOf(int modelRow);
	}

	private final ViewStack viewStack;
	private final Source source;
	private final DefaultTableModel model;
	private final Table table;
	private final javax.swing.JTable jtable;
	private final JScrollPane scroll;
	/** Index of the trailing Buy/Stream column appended to the model. */
	private final int buyStreamCol;
	/** Index of the optional library-membership (✓/＋) column, or -1 if absent. */
	private final int libToggleCol;

	// ── Optional grouped presentation ───────────────────────────────────────────
	private GroupedListPanel groupedPanel;
	private List<Grouping> cachedGroupings;
	private Grouping currentGrouping;
	private boolean grouped;

	/**
	 * @param viewStack          used to navigate to a store when "Buy" is chosen
	 * @param columns            the app's own column headers; the managed
	 *                           (library-toggle / Buy-Stream) columns are appended
	 * @param showLibraryToggle  whether to include the ✓/＋ membership column
	 * @param source             per-row playback metadata and callbacks
	 */
	public MusicTable(ViewStack viewStack, String[] columns, boolean showLibraryToggle, Source source) {
		super(new BorderLayout());
		this.viewStack = viewStack;
		this.source = source;
		setOpaque(false);

		// A trailing Buy/Stream column is appended to every table; its cells are
		// rendered from per-row availability, not the row data, so rows added with
		// the caller's column count are padded to fit. An optional library-toggle
		// column precedes it.
		String[] cols = new String[columns.length + (showLibraryToggle ? 1 : 0) + 1];
		System.arraycopy(columns, 0, cols, 0, columns.length);
		int idx = columns.length;
		libToggleCol = showLibraryToggle ? idx++ : -1;
		buyStreamCol = idx;
		model = new DefaultTableModel(cols, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
			@Override
			public void addRow(Object[] rowData) {
				if (rowData != null && rowData.length < getColumnCount()) {
					Object[] padded = new Object[getColumnCount()];
					System.arraycopy(rowData, 0, padded, 0, rowData.length);
					rowData = padded;
				}
				super.addRow(rowData);
			}
		};
		table = new Table(model);
		jtable = table.getComponent();
		jtable.setFillsViewportHeight(true);
		jtable.setShowGrid(false);
		jtable.setIntercellSpacing(new Dimension(0, 0));
		jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = jtable.rowAtPoint(e.getPoint());
				if (row < 0)
					return;
				int col = jtable.columnAtPoint(e.getPoint());
				if (e.getClickCount() == 2) {
					activate(row);
				} else if (e.getClickCount() == 1) {
					if (col == libToggleCol && handleLibraryToggleClick(row)) return;
					if (col == buyStreamCol && handleBuyStreamClick(row, e)) return;
					source.onCellClicked(row, col);
				}
			}

			@Override public void mousePressed(MouseEvent e)  { maybeShowRowMenu(e); }
			@Override public void mouseReleased(MouseEvent e) { maybeShowRowMenu(e); }
		});

		ThemedTableCellRenderer renderer = new ThemedTableCellRenderer();
		for (int i = 0; i < jtable.getColumnCount(); i++) {
			jtable.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		// The Buy/Stream column draws its own split button.
		TableColumn bs = jtable.getColumnModel().getColumn(buyStreamCol);
		bs.setCellRenderer(new BuyStreamRenderer());
		bs.setResizable(false);
		bs.setMinWidth(0);
		bs.setMaxWidth(BUY_STREAM_COL_WIDTH);
		bs.setPreferredWidth(BUY_STREAM_COL_WIDTH);
		// Optional library-membership (✓/＋) toggle column.
		if (libToggleCol >= 0) {
			TableColumn lt = jtable.getColumnModel().getColumn(libToggleCol);
			lt.setCellRenderer(new LibraryToggleRenderer());
			lt.setResizable(false);
			lt.setMinWidth(0);
			lt.setMaxWidth(LIB_TOGGLE_COL_WIDTH);
			lt.setPreferredWidth(LIB_TOGGLE_COL_WIDTH);
		}

		scroll = new JScrollPane(jtable);
		// Non-UIResource empty border so the Nimbus reinstall on theme change
		// doesn't re-install a default scroll-pane border.
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(true);
		scroll.getViewport().setOpaque(true);
		add(scroll, BorderLayout.CENTER);

		updateColors();
		ThemeManager.addChangeListener(this::updateColors);
		// Repaint so the now-playing row highlight follows the active track.
		PlayQueue.getInstance().addChangeListener(jtable::repaint);
	}

	// ── Public API ──────────────────────────────────────────────────────────────

	/** The table model to fill; pad-tolerant of the caller's own column count. */
	public DefaultTableModel getModel() { return model; }

	/** The underlying table (for selection queries and repaints). */
	public Table getTable() { return table; }

	/** The selected model row, or -1. */
	public int getSelectedRow() { return jtable.getSelectedRow(); }

	/**
	 * Call after the model has been (re)filled: sizes the managed columns (hiding
	 * them when no row is playable) and, if the grouped view is showing, rebuilds
	 * its sections.
	 */
	public void refresh() {
		boolean any = false;
		for (int r = 0; r < model.getRowCount(); r++) {
			if (source.playRequestAt(r) != null) { any = true; break; }
		}
		sizeColumn(buyStreamCol, any ? BUY_STREAM_COL_WIDTH : 0);
		if (libToggleCol >= 0) sizeColumn(libToggleCol, any ? LIB_TOGGLE_COL_WIDTH : 0);
		if (grouped)
			rebuildGroups();
	}

	/** The grouping options offered by the source (cached after first read). */
	public List<Grouping> groupings() {
		if (cachedGroupings == null)
			cachedGroupings = new ArrayList<>(source.groupings());
		return cachedGroupings;
	}

	public boolean isGrouped() { return grouped; }

	/** Choose the active grouping; rebuilds immediately when grouped. */
	public void setGrouping(Grouping g) {
		currentGrouping = g;
		if (grouped)
			rebuildGroups();
	}

	/** Switch between the flat table and the grouped sections. */
	public void setGrouped(boolean on) {
		grouped = on;
		if (on) {
			if (groupedPanel == null)
				groupedPanel = new GroupedListPanel();
			if (currentGrouping == null && !groupings().isEmpty())
				currentGrouping = groupings().get(0);
			rebuildGroups();
			scroll.setViewportView(groupedPanel.getComponent());
		} else {
			scroll.setViewportView(jtable);
		}
		scroll.revalidate();
		scroll.repaint();
	}

	// ── Activation & context menu ────────────────────────────────────────────────

	/**
	 * Turn the visible rows into the play queue and start at {@code row}. Every
	 * playable row (per {@link Source#queueItemAt}) becomes a queue entry; if the
	 * source produces none, falls back to {@link Source#onActivate(int)}.
	 */
	private void activate(int row) {
		List<PlayQueueItem> queue = new ArrayList<>();
		int startIndex = -1;
		for (int i = 0; i < model.getRowCount(); i++) {
			PlayQueueItem item = source.queueItemAt(i);
			if (item == null)
				continue;
			if (i == row)
				startIndex = queue.size();
			queue.add(item);
		}
		if (startIndex >= 0) {
			PlayQueue.getInstance().setQueueAndPlay(queue, startIndex);
		} else {
			source.onActivate(row);
		}
	}

	/**
	 * Pop the row context menu ("Play with…") on a platform popup trigger, when the
	 * row under the cursor is playable.
	 */
	private void maybeShowRowMenu(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;
		int row = jtable.rowAtPoint(e.getPoint());
		if (row < 0)
			return;
		jtable.setRowSelectionInterval(row, row);
		PlayRequest req = source.playRequestAt(row);
		if (req == null)
			return;
		JPopupMenu menu = new JPopupMenu();
		JMenuItem playWith = new JMenuItem("Play with…");
		playWith.addActionListener(a -> PlaybackCoordinator.resolveAndPlay(req, true));
		menu.add(playWith);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	// ── Library-membership toggle (✓/＋) ──────────────────────────────────────────

	/** Toggle library membership for a clicked row; repaints so the glyph flips. */
	private boolean handleLibraryToggleClick(int row) {
		if (row < 0 || row >= model.getRowCount() || source.playRequestAt(row) == null) return false;
		source.toggleLibraryAt(row);
		jtable.repaint();
		return true;
	}

	/** Renders the library-membership glyph (✓ in library / ＋ to add), themed. */
	private final class LibraryToggleRenderer extends ThemedTableCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
		public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
				boolean focus, int row, int column) {
			super.getTableCellRendererComponent(t, value, sel, focus, row, column);
			boolean playable = source.playRequestAt(row) != null;
			boolean in = playable && source.inLibraryAt(row);
			setHorizontalAlignment(CENTER);
			setText(!playable ? "" : (in ? "✓" : "＋"));
			if (!sel) setForeground(in ? ThemeManager.getAccentBackgroundColor() : new Color(150, 150, 150));
			return this;
		}
	}

	// ── Buy/Stream split button ──────────────────────────────────────────────────

	/** Handle a click in the Buy/Stream cell: ▾ zone (or Buy-only) opens the menu,
	 *  the left part plays when streamable. Returns true if it consumed the click. */
	private boolean handleBuyStreamClick(int row, MouseEvent e) {
		PlayRequest req = source.playRequestAt(row);
		if (req == null) return false;
		TrackAvailability a = AvailabilityResolver.get().availabilityFor(req, jtable::repaint);
		if (a.local()) { activate(row); return true; }   // already have the file → play it
		Rectangle cell = jtable.getCellRect(row, buyStreamCol, false);
		boolean onArrow = (e.getX() - cell.x) >= cell.width - BUY_STREAM_ARROW_W - 4;
		if (onArrow || !a.hasStream()) {
			showBuyStreamMenu(row, req, a);
		} else {
			activate(row);   // default action: stream/play
		}
		return true;
	}

	private void showBuyStreamMenu(int row, PlayRequest req, TrackAvailability a) {
		JPopupMenu menu = new JPopupMenu();

		if (a.hasStream()) {
			JMenu stream = new JMenu("Stream");
			for (se.spacify.app.music.service.MusicService ms : a.streamServices()) {
				JMenuItem it = new JMenuItem("Play on " + ms.getName());
				it.addActionListener(x -> PlaybackCoordinator.playOn(ms, req));
				stream.add(it);
			}
			menu.add(stream);
		}

		JMenu buy = new JMenu("Buy");
		String query = ((req.title() == null ? "" : req.title())
			+ (req.artist() == null || req.artist().isBlank() ? "" : " " + req.artist())).trim();
		for (se.spacify.web.StoreCatalog.Store s : se.spacify.web.StoreCatalog.STORES) {
			JMenuItem it = new JMenuItem(s.name());
			it.addActionListener(x -> viewStack.navigate(s.searchUri(query)));
			buy.add(it);
		}
		menu.add(buy);

		menu.addSeparator();
		JMenuItem openWith = new JMenuItem("Open with…");
		openWith.addActionListener(x -> PlaybackCoordinator.resolveAndPlay(req, true));
		menu.add(openWith);

		Rectangle cell = jtable.getCellRect(row, buyStreamCol, false);
		menu.show(jtable, cell.x, cell.y + cell.height);
	}

	/**
	 * Cell renderer driven by {@link TrackAvailability}: a real {@link se.spacify.controls.Button}
	 * (so it uses the same skinned painting as every other button, via SpaceButtonUI)
	 * labelled "Stream ▾" / "Buy ▾"; or, when the track is matched locally, just a
	 * file icon and no button.
	 */
	private final class BuyStreamRenderer implements javax.swing.table.TableCellRenderer {
		private final BuyStreamButton button = new BuyStreamButton();
		private final FileCell        fileCell = new FileCell();
		private final JLabel          blank = new JLabel();
		@Override
		public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
				boolean focus, int row, int column) {
			PlayRequest req = source.playRequestAt(row);
			if (req == null) return blank;
			TrackAvailability a = AvailabilityResolver.get().availabilityFor(req, jtable::repaint);
			if (a.local()) return fileCell;   // have the file → file icon only, no button
			button.setFont(t.getFont());
			button.setText(!a.resolved() ? "…" : (a.hasStream() ? "Stream ▾" : "Buy ▾"));
			return button.getComponent();
		}
	}

	/** A {@link se.spacify.controls.Button} usable as a detached table cell renderer —
	 *  falls back to the live window so the skin (SpaceButtonUI paint) still resolves. */
	private static final class BuyStreamButton extends se.spacify.controls.Button {
		private static final long serialVersionUID = 1L;
		BuyStreamButton() {
			super();
			component.setFocusable(false);
			component.setMargin(new java.awt.Insets(0, 0, 0, 0));
		}
	}

	/** A file glyph shown when the track already has a local copy. */
	private static final class FileCell extends JComponent {
		private static final long serialVersionUID = 1L;
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = 9, h = 11, fold = 3;
			int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
			g2.setColor(new Color(150, 200, 255));
			g2.fillPolygon(new int[]{ x, x + w - fold, x + w, x + w, x },
			               new int[]{ y, y, y + fold, y + h, y + h }, 5);
			g2.dispose();
		}
	}

	// ── Grouped presentation ─────────────────────────────────────────────────────

	/** Rebuild the grouped sections from the current model rows. */
	private void rebuildGroups() {
		if (groupedPanel == null)
			return;
		Grouping g = currentGrouping;
		if (g == null) {
			groupedPanel.setGroups(List.of());
			return;
		}
		// Preserve model row order within each section (rows already arrive in
		// album order), and section order by first appearance.
		Map<String, GroupBucket> buckets = new LinkedHashMap<>();
		for (int row = 0; row < model.getRowCount(); row++) {
			PlayQueueItem item = source.queueItemAt(row);
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

	// ── Helpers ──────────────────────────────────────────────────────────────────

	private void sizeColumn(int index, int width) {
		TableColumn col = jtable.getColumnModel().getColumn(index);
		col.setMinWidth(0);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
	}

	/** Format a millisecond duration as {@code m:ss} ("" for non-positive). */
	public static String fmtDuration(long ms) {
		if (ms <= 0)
			return "";
		long s = ms / 1000;
		return String.format("%d:%02d", s / 60, s % 60);
	}

	private void updateColors() {
		Color bg = ThemeManager.getBackground();
		Color fg = ThemeManager.getForeground();
		Color grid = ThemeManager.getGridColor();

		jtable.setBackground(bg);
		jtable.setForeground(fg);
		jtable.setGridColor(grid);
		scroll.setBackground(bg);
		scroll.getViewport().setBackground(bg);
		jtable.repaint();
	}
}
