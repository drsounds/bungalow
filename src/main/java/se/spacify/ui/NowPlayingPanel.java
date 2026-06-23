package se.spacify.ui;

import se.spacify.controls.Table;
import se.spacify.controls.ToolBar;
import se.spacify.navigation.ViewStack;
import se.spacify.app.media.service.MediaService;
import se.spacify.app.media.service.MediaServicePlayerComponent;
import se.spacify.service.media.PlaybackCoordinator;
import se.spacify.service.media.PlayQueue;
import se.spacify.service.media.PlayQueueItem;
import se.spacify.service.media.PlayRequest;
import se.spacify.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Right-hand play-queue panel. Mirrors the library table styling (theme colours,
 * striped rows, accent selection) but lists the active {@link PlayQueue} with
 * Name / Artists / Duration columns. The currently-playing entry is highlighted,
 * and double-clicking a row jumps playback to that position.
 */
public class NowPlayingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
	private final DefaultTableModel model;
    private final Table            table;
    private final JScrollPane       scroll;
    private final JLabel            emptyLabel;
	private JToolBar topToolbar;
	private JToolBar bottomToolbar;
	/** Hosts the active Service's player surface, if any, at the bottom of the panel. */
	private final JPanel playerHost = new JPanel(new BorderLayout());
	private MediaServicePlayerComponent currentPlayer;

    public NowPlayingPanel(ViewStack viewStack) {
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(220, 0));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        
        topToolbar = new ToolBar();
        topToolbar.setFloatable(false);
        topToolbar.setOpaque(true);
        topToolbar.setBackground(ThemeManager.getTintColor());
        add(topToolbar);

        JButton title = new JButton(UIManager.getIcon("FileView.fileIcon"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 11f));
        
        // ── Queue table ──────────────────────────────────────────────────────
        model = new DefaultTableModel(new String[]{"Name", "Duration"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new Table(model);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(1).setMaxWidth(64);
        // Transparent so the panel's gradient shows through behind the rows.
        table.setOpaque(false);

        QueueCellRenderer renderer = new QueueCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) PlayQueue.getInstance().playAt(row);
                }
            }

            @Override public void mousePressed(MouseEvent e)  { maybeShowQueueMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShowQueueMenu(e); }
        });

        scroll = new JScrollPane(table);
        // Non-UIResource empty border so the Nimbus reinstall on theme change
        // doesn't re-install a default scroll-pane border.
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // Transparent viewport/scroll-pane so the gradient shows behind the rows.
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // Repaint the whole viewport on scroll so the gradient doesn't smear.
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        emptyLabel = new JLabel("Nothing playing");
        emptyLabel.setForeground(Color.WHITE);
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(12f));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(topToolbar,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        bottomToolbar = new ToolBar();
        bottomToolbar.setFloatable(false);
        bottomToolbar.setOpaque(true);
        bottomToolbar.setBackground(ThemeManager.getTintColor());
        bottomToolbar.add(new JButton("Sync"));

        // The active media Service's player surface sits below the queue and above
        // the toolbar; it's part of this sticky panel, so navigation never hides it.
        playerHost.setOpaque(false);
        playerHost.setVisible(false);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.PAGE_AXIS));
        south.add(playerHost);
        south.add(bottomToolbar);
        add(south, BorderLayout.SOUTH);

        topToolbar.add(title);

        updateColors();
        refresh();
        setActivePlayer(PlaybackCoordinator.getActiveService());

        PlayQueue.getInstance().addChangeListener(() ->
            SwingUtilities.invokeLater(this::refresh));
        ThemeManager.addChangeListener(() -> { updateColors(); refresh(); });
        // Swap in the player surface of whichever Service is handling the current play.
        PlaybackCoordinator.addActiveServiceListener(ms ->
            SwingUtilities.invokeLater(() -> setActivePlayer(ms)));
    }

    /**
     * Show {@code Service}'s player component (if it has one) in the bottom host,
     * replacing any previous one. Components get {@code onDeactivated}/
     * {@code onActivated} callbacks around the swap.
     */
    /** Right-click a queue entry to re-pick which Service plays it ("Play with…"). */
    private void maybeShowQueueMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);
        List<PlayQueueItem> items = PlayQueue.getInstance().getItems();
        if (row >= items.size()) return;
        PlayRequest req = items.get(row).getSource();
        if (req == null) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem playWith = new JMenuItem("Play with…");
        playWith.addActionListener(a -> PlaybackCoordinator.resolveAndPlay(req, true));
        menu.add(playWith);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void setActivePlayer(MediaService Service) {
        MediaServicePlayerComponent next = Service != null ? Service.getPlayerComponent() : null;
        if (next == currentPlayer) return;

        if (currentPlayer != null) {
            currentPlayer.onDeactivated();
            playerHost.remove(currentPlayer);
        }
        currentPlayer = next;
        if (currentPlayer != null) {
            playerHost.add(currentPlayer, BorderLayout.CENTER);
            currentPlayer.onActivated();
        }
        playerHost.setVisible(currentPlayer != null);
        playerHost.revalidate();
        playerHost.repaint();
    }

    /** Rebuild the table rows from the current queue and keep the selection on the playing row. */
    private void refresh() {
        List<PlayQueueItem> items = PlayQueue.getInstance().getItems();
        model.setRowCount(0);
        for (PlayQueueItem it : items) {
            model.addRow(new Object[]{
                it.getName(), fmtDuration(it.getDurationMs())
            });
        }
        int current = PlayQueue.getInstance().getCurrentIndex();
        if (current >= 0 && current < model.getRowCount()) {
            table.setRowSelectionInterval(current, current);
        } else {
            table.clearSelection();
        }

        boolean empty = items.isEmpty();
        // Show the placeholder in the centre while the queue is empty.
        if (empty && scroll.getParent() == this) {
            remove(scroll);
            add(emptyLabel, BorderLayout.CENTER);
        } else if (!empty && emptyLabel.getParent() == this) {
            remove(emptyLabel);
            add(scroll, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private void updateColors() {
        // This sticky panel always renders white text on its accent gradient,
        // regardless of the light/dark theme setting.
        Color grid = ThemeManager.getGridColor();
        table.setForeground(Color.WHITE);
        table.setGridColor(grid);
        table.repaint();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);   // we paint our own gradient in paintComponent
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ((MainWindow)(SwingUtilities.getWindowAncestor(this))).getSkin().paintPlaylist(this, g2);
        
        g2.dispose();
    }

    private static String fmtDuration(long ms) {
        if (ms <= 0) return "";
        long s = ms / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    /**
     * Renderer that keeps the gradient visible and never stripes rows. The playing
     * row keeps its distinct light-green-on-black highlight; every other row is
     * transparent with white text, regardless of the light/dark theme.
     */
    private static final class QueueCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 5721808300261876002L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean playing = row == PlayQueue.getInstance().getCurrentIndex();
            if (playing) {
                setOpaque(true);
                setBackground(ThemeManager.getNowPlayingBackground());
                setForeground(ThemeManager.getNowPlayingForeground());
            } else {
                setOpaque(false);   // let the gradient show through
                setForeground(Color.WHITE);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
        }
    }
}
