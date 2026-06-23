package se.spacify.ui;

import se.spacify.controls.MenuToolButton;
import se.spacify.controls.ToolBar;
import se.spacify.controls.ToolMenuItem;
import se.spacify.library.LibraryEvents;
import se.spacify.navigation.NavigationListener;
import se.spacify.navigation.ViewStack;
import se.spacify.app.library.views.LibraryScanAction;
import se.spacify.navigation.SidebarNode;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.controls.Panel;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import se.spacify.controls.Tree;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LeftLibraryMenu extends Panel implements NavigationListener {

    private final Tree tree;
    private final JScrollPane scroll;
    private final DefaultMutableTreeNode root;
    private boolean suppressSelection = false;
	private ToolBar toolbar;
	private JTextField searchField;
	private ToolBar bottomToolbar;
	private MenuToolButton addToLibraryMenuButton;
	@SuppressWarnings("unused")
	private JPopupMenu addToLibraryMenu;
    private ViewStack viewStack;

	public ViewStack getViewStack() {
        return viewStack;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        getMainWindow().getSkin().paintLeftLibraryMenu(this, g2);
        g2.dispose();
        super.paintComponent(g);
    }
    

    public void reload() {
		
	}
	
	public void onAdd() {
		
	}
	
    public LeftLibraryMenu(ViewStack viewStack) {
        this.viewStack = viewStack;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setOpaque(true);

        // Core shell nodes only; content subtrees (Your Library, Sites) are
        // contributed by built-in plugins via addSidebarNode() at activation.
        root = new DefaultMutableTreeNode("root");
        root.add(nodeFor(new SidebarNode("Player",    "spacify:now-playing")));
        root.add(nodeFor(new SidebarNode("Downloads", "spacify:downloads")));
        root.add(nodeFor(new SidebarNode("Apps",   "spacify:apps")));

        toolbar = new ToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(true);
        toolbar.setBackground(ThemeManager.getTintColor());
        add(toolbar, BorderLayout.NORTH);
        

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search...");
        searchField.setPreferredSize(new Dimension(180, 28));
        searchField.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (!q.isEmpty()) {
                String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
                viewStack.navigate("spacify:search?q=" + encoded);
            }
        });
        toolbar.add(searchField);


        tree = new Tree(root);
        tree.setOpaque(false);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        tree.setRowHeight(28);
        tree.setCellRenderer(new ThemedTreeCellRenderer());

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Resolve the row from the y-position only, so a click anywhere along
                // the row width counts — not just on the label text.
                int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                if (row < 0) return;
                Rectangle bounds = tree.getRowBounds(row);
                if (bounds == null || e.getY() < bounds.y || e.getY() >= bounds.y + bounds.height) {
                    return;   // clicked in the empty area below the last row
                }
                tree.setSelectionRow(row);
                TreePath path = tree.getPathForRow(row);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof SidebarNode sn && sn.getUri() != null) {
                    suppressSelection = true;
                    viewStack.navigate(sn.getUri());
                    suppressSelection = false;
                } else if (!node.isLeaf()) {
                    // A folder row (no URI): toggle it across the whole width.
                    if (tree.isExpanded(row)) tree.collapseRow(row); else tree.expandRow(row);
                }
            }
        });

        scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // Keep scroll and viewport opaque so they paint a background;
        // colours are kept in sync by updateColors() below.
        scroll.setOpaque(true);
        scroll.getViewport().setOpaque(true);
        add(scroll, BorderLayout.CENTER);

        bottomToolbar = new ToolBar();
        // Cross-plugin download activity spinner (listens on the broadcast bus).
        bottomToolbar.add(new DownloadActivityIndicator());
        add(bottomToolbar, BorderLayout.SOUTH);
        
        addToLibraryMenuButton = new MenuToolButton();
        addToLibraryMenuButton.setText("Add to Library");
        addToLibraryMenu = addToLibraryMenuButton.getPopup();
        ToolMenuItem addBtn    = new ToolMenuItem("Add");
        ToolMenuItem scanBtn   = new ToolMenuItem("Scan…");

        scanBtn.addActionListener(e -> LibraryScanAction.run(this,
            () -> { /*reload();*/ LibraryEvents.fireChanged(); }));
        addBtn.addActionListener(e -> { onAdd(); reload(); LibraryEvents.fireChanged(); });
        addToLibraryMenuButton.add(addBtn);
        addToLibraryMenuButton.add(scanBtn);
        
        bottomToolbar.add(addToLibraryMenuButton);
        	
        viewStack.addNavigationListener(this);

        updateColors();
        ThemeManager.addChangeListener(this::updateColors);
    }

    // ── Theme-aware colour sync ───────────────────────────────────────────────

    private void updateColors() {
        Color bg = ThemeManager.getBackground();
        setBackground(bg);
        tree.setBackground(bg);
        scroll.setBackground(bg);
        scroll.getViewport().setBackground(bg);
        tree.repaint();
    }

    // ── Custom cell renderer ──────────────────────────────────────────────────

    private static final class ThemedTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = -3474131119330495091L;

		@Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            // Transparent: the full-width selection band is painted by the Tree, so the
            // renderer must not paint its own label-width background box or focus border.
            setOpaque(false);
            setBackgroundSelectionColor(null);
            setBackgroundNonSelectionColor(null);
            setBorderSelectionColor(null);
            setForeground(sel ? Color.WHITE : ThemeManager.getForeground());
            // Per-node favicon (bookmarks); falls back to the default tree icon.
            if (value instanceof DefaultMutableTreeNode n
                    && n.getUserObject() instanceof SidebarNode sn && sn.getIcon() != null) {
                setIcon(sn.getIcon());
            }
            return this;
        }
    }

    // ── LeftLibraryMenu tree model ────────────────────────────────────────────────────

    private DefaultMutableTreeNode nodeFor(SidebarNode sn) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sn);
        for (SidebarNode child : sn.getChildren()) node.add(nodeFor(child));
        return node;
    }

    public DefaultMutableTreeNode getRootNode() { return root; }

    /** Append a plugin-contributed node (subtree) to the sidebar root, live. */
    public DefaultMutableTreeNode addSidebarNode(SidebarNode sn) {
        DefaultMutableTreeNode node = nodeFor(sn);
        root.add(node);
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(root);
        return node;
    }

    /** Remove a node previously added via {@link #addSidebarNode}. */
    public void removeSidebarNode(DefaultMutableTreeNode node) {
        if (node.getParent() != null) {
            root.remove(node);
            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(root);
        }
    }

    /** Replace a node's children (used for dynamic plugin subtrees). */
    public void setNodeChildren(DefaultMutableTreeNode node, List<SidebarNode> children) {
        node.removeAllChildren();
        for (SidebarNode sn : children) node.add(nodeFor(sn));
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
    }

    /** Expand a node in the tree. */
    public void expandNode(DefaultMutableTreeNode node) {
        tree.expandPath(new TreePath(node.getPath()));
    }

    // ── NavigationListener ────────────────────────────────────────────────────

    @Override
    public void onNavigate(String uri, boolean canGoBack, boolean canGoForward) {
        if (suppressSelection) return;
        selectNodeForUri(uri, (DefaultMutableTreeNode) tree.getModel().getRoot());
    }

    private boolean selectNodeForUri(String uri, DefaultMutableTreeNode node) {
        if (node.getUserObject() instanceof SidebarNode sn && uri.equals(sn.getUri())) {
            TreePath path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (selectNodeForUri(uri, (DefaultMutableTreeNode) node.getChildAt(i))) return true;
        }
        return false;
    }
}
