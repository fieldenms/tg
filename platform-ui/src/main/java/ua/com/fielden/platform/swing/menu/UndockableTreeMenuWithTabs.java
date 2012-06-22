package ua.com.fielden.platform.swing.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.swing.view.ICloseHook;

import com.jidesoft.swing.JideTabbedPane;

public class UndockableTreeMenuWithTabs<V extends BaseNotifPanel> extends TreeMenuWithTabs<V> {

    private static final long serialVersionUID = 2081673364500267565L;

    /**
     * List of frames containing undocked tree menu items.
     */
    private final List<UndockTreeMenuItemFrame> undockedFrames;

    private final Action undockTreeItemAction, dockTreeItemAction;

    private final JCheckBoxMenuItem visibilityItem;

    private final JPopupMenu popupMenu;

    private final BlockingIndefiniteProgressLayer treeProgressLayer;

    /**
     * A convenient constructor, which results in creation of a tree menu with default info panel and undockable tab sheets.
     *
     * @param menuItem
     * @param menuFilter
     * @param blockingPane
     */
    public UndockableTreeMenuWithTabs(final TreeMenuItem<V> menuItem, final IFilter menuFilter, final IUserProvider userProvider, final BlockingIndefiniteProgressPane blockingPane) {
	this(menuItem, menuFilter, userProvider, null, blockingPane);
    }

    /**
     * Principle constructor.
     *
     * @param menuItem
     *            -- the root of the tree menu.
     * @param menuFilter
     *            -- filter used for user driven filtering of menu items
     * @param defaultInforPanel
     *            -- an info panel used in cases where individual menu items were not provided with their own info panels.
     * @param blockingPane
     *            -- the pane used for blocking UI upon activation of menu items
     */
    public UndockableTreeMenuWithTabs(final TreeMenuItem<V> menuItem, final IFilter menuFilter, final IUserProvider userProvider, final JPanel defaultInforPanel, final BlockingIndefiniteProgressPane blockingPane) {
	super(menuItem, menuFilter, defaultInforPanel, new HolderPanelWithUndockedFrame(), blockingPane);

	// initialising the detached frames.
	undockedFrames = ((HolderPanelWithUndockedFrame) getHolder()).getUndockableFrames();

	treeProgressLayer = new BlockingIndefiniteProgressLayer(null, "Saving the tree menu...");

	// Creates action those allows to attache or detach tree menu items
	// Also creates pop up menu for the this tree, registers action in the action map
	undockTreeItemAction = createUndockTreeItemAction();
	dockTreeItemAction = createDockTreeItemAction();
	visibilityItem = new JCheckBoxMenuItem("Visible");
	visibilityItem.addActionListener(createInvisibilityActionListener());
	popupMenu = new JPopupMenu();
	popupMenu.add(dockTreeItemAction);
	popupMenu.add(undockTreeItemAction);
	if (userProvider.getUser().isBase()) {
	    popupMenu.add(visibilityItem);
	}
	setCellRenderer(new FilterCellRenderer(getModel()) {
	    @Override
	    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		final String oldText = TitlesDescsGetter.removeItalic(getText());
		if (!isMenuItemVisible((TreeMenuItem) value)) {
		    setText(TitlesDescsGetter.italic(oldText));
		} else {
		    setText(oldText);
		}
		return this;
	    }
	});
	final PopupMenuListener listener = new PopupMenuListener();
	addMouseListener(listener);
	final String DOCK_ITEM = "Dock selected item action";
	final String UNDOCK_ITEM = "Undock selected item action";
	final String UNDOCK_SELECTED_TAB = "Undock selected tab action";
	getActionMap().put(UNDOCK_ITEM, undockTreeItemAction);
	getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), UNDOCK_ITEM);
	getActionMap().put(DOCK_ITEM, dockTreeItemAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), DOCK_ITEM);
	getTabPane().getActionMap().put(UNDOCK_SELECTED_TAB, createUndockSelectedTabSheetAction());
	getTabPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), UNDOCK_SELECTED_TAB);
    }

    private static class HolderPanelWithUndockedFrame extends BasePanel {

	private static final long serialVersionUID = -7128374800302043868L;

	private final List<UndockTreeMenuItemFrame> undockableFrames = new ArrayList<UndockTreeMenuItemFrame>();

	public HolderPanelWithUndockedFrame() {
	    super(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));
	    setBorder(new LineBorder(new Color(140, 140, 140)));
	}

	public List<UndockTreeMenuItemFrame> getUndockableFrames() {
	    return undockableFrames;
	}

	@Override
	public String getInfo() {
	    return "Panel that holds all opened reports";
	}

	@Override
	public String whyCannotClose() {
	    return "Please save or cancel all changes";
	}

	@Override
	public ICloseGuard canClose() {
	    final JideTabbedPane tabPanel = getComponent(0) instanceof JideTabbedPane ? (JideTabbedPane) getComponent(0) : null;
	    if (tabPanel == null) {
		return null;
	    }
	    int shiftTabCount = 0;
	    while (tabPanel.getTabCount() - shiftTabCount > 0) {
		final Component componentAt = tabPanel.getComponentAt(shiftTabCount);
		ICloseGuard result;
		if (componentAt instanceof ICloseGuard) {
		    result = ((ICloseGuard) componentAt).canClose();
		    if (result == null) {
			if (componentAt instanceof BaseNotifPanel) {
			    ((BaseNotifPanel) componentAt).getAssociatedTreeMenuItem().setState(TreeMenuItemState.ALL);
			    ((BaseNotifPanel) componentAt).close();
			}
			tabPanel.removeTabAt(shiftTabCount);
		    } else {
			return result;
		    }
		} else {
		    shiftTabCount++;
		}
	    }
	    final Iterator<UndockTreeMenuItemFrame> frameIterator = undockableFrames.iterator();
	    while (frameIterator.hasNext()) {
		final UndockTreeMenuItemFrame undockedItem = frameIterator.next();
		final ICloseGuard guard = undockedItem.canClose();
		if (guard == null) {
		    undockedItem.close();
		} else {
		    return guard;
		}
	    }
	    return null;
	}

    }

    /**
     * Creates {@link Action} that attaches {@link TreeMenuItem} to the tab panel.
     *
     * @return
     */
    private Action createDockTreeItemAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = -1839706524589824722L;

	    {
		putValue(Action.NAME, "Open in tab");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final TreeMenuItem<?> treeMenuItem = getSelectedItem();
		if (treeMenuItem != null) {
		    activateItem(treeMenuItem, true);
		}
	    }

	};
    }

    /**
     * Creates {@link Action} that detaches selected {@link TreeMenuItem}.
     *
     * @return
     */
    private Action createUndockTreeItemAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = -1839706524589824722L;

	    {
		putValue(Action.NAME, "Open in window");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final TreeMenuItem<?> treeMenuItem = getSelectedItem();
		if (treeMenuItem != null) {
		    activateItem(treeMenuItem, false);
		}
	    }

	};
    }

    private Action createUndockSelectedTabSheetAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = -8376589711459657749L;

	    @SuppressWarnings("rawtypes")
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final TreeMenuItem<?> treeMenuItem = getTabPane().getSelectedComponent() instanceof BaseNotifPanel ? ((BaseNotifPanel) getTabPane().getSelectedComponent()).getAssociatedTreeMenuItem()
			: null;
		if (treeMenuItem != null) {
		    activateItem(treeMenuItem, false);
		}
	    }

	};
    }

    @Override
    protected void activateItem(final TreeMenuItem<?> item) {
	// check if this menu item's view is already present amongst tabs
	int itemIndex = -1;
	switch (item.getState()) {
	case DOCK:
	    itemIndex = menuItemTab(item);
	    getTabPane().setSelectedIndex(itemIndex);
	    selectMenuItem(item);
	    if (item.hasInfoPanel()) {
		getTabPane().setComponentAt(0, item.getInfoPanel());
	    }
	    break;
	case UNDOCK:
	    itemIndex = menuItemFrame(item);
	    final UndockTreeMenuItemFrame frame = undockedFrames.get(itemIndex);
	    frame.setVisible(true);
	    selectMenuItem(item);
	    if (item.hasInfoPanel()) {
		getTabPane().setComponentAt(0, item.getInfoPanel());
	    }
	case ALL:
	case NONE:
	    // item's view is not yet present -- activate a corresponding info panel
	    if (item.hasInfoPanel()) {
		getTabPane().setComponentAt(0, item.getInfoPanel());
	    } else {
		getTabPane().setComponentAt(0, getDefaultInfoPanel());
	    }
	    getTabPane().setSelectedIndex(0);
	    getTabPane().updateUI();
	}

    }

    private int menuItemFrame(final TreeMenuItem<?> item) {
	for (int frameIndex = 0; frameIndex < undockedFrames.size(); frameIndex++) {
	    if (undockedFrames.get(frameIndex).getView() == item.getView()) {
		return frameIndex;
	    }

	}
	return -1;
    }

    /**
     * Activates specified {@link TreeMenuItem} and sets specified state for it.
     *
     * @param item
     * @param state
     */
    private void activateItem(final TreeMenuItem<?> item, final boolean dock) {
	if (item == null) {
	    return;
	}
	switch (item.getState()) {
	case DOCK:
	    activateDockedTreeItem(item, dock);
	    break;
	case UNDOCK:
	    activateUndockedTreeItem(item, dock);
	    break;
	case ALL:
	    openItem(item, dock);
	    selectMenuItem(item);
	    break;
	}

    }

    private void openItem(final TreeMenuItem<?> item, final boolean dock) {
	if (dock) {
	    getTabPane().addTab(item.toString(), item.getView());
	    getTabPane().setSelectedIndex(getTabPane().getTabCount() - 1);
	    item.setState(TreeMenuItemState.DOCK);
	    // initialisation must occur after a new tab is selected to ensure correct focus traversal
	    item.getView().getModel().init(getBlockingPane(), item.getView());
	} else {
	    final UndockTreeMenuItemFrame undockedItem = createUndockedFrame(item);
	    undockedFrames.add(undockedItem);
	    item.setState(TreeMenuItemState.UNDOCK);
	    undockedItem.pack();
	    RefineryUtilities.centerFrameOnScreen(undockedItem);
	    undockedItem.setVisible(true);
	    item.getView().getModel().init(undockedItem.getBlockingPane(), item.getView());
	}

    }

    private void activateUndockedTreeItem(final TreeMenuItem<?> item, final boolean dock) {
	if (dock) {
	    final int frameIndex = menuItemFrame(item);
	    final UndockTreeMenuItemFrame undockedItem = undockedFrames.remove(frameIndex);
	    item.setState(TreeMenuItemState.DOCK);
	    undockedItem.setVisible(false);
	    undockedItem.dispose();
	    getTabPane().addTab(item.toString(), item.getView());
	    getTabPane().setSelectedIndex(getTabPane().getTabCount() - 1);
	}
    }

    private void activateDockedTreeItem(final TreeMenuItem<?> item, final boolean dock) {
	if (!dock) {
	    final UndockTreeMenuItemFrame undockedItem = createUndockedFrame(item);
	    undockedFrames.add(undockedItem);
	    item.setState(TreeMenuItemState.UNDOCK);
	    undockedItem.pack();
	    RefineryUtilities.centerFrameOnScreen(undockedItem);
	    undockedItem.setVisible(true);
	}
    }

    private UndockTreeMenuItemFrame createUndockedFrame(final TreeMenuItem<?> treeMenuItem) {
	final UndockTreeMenuItemFrame undockedItem = new UndockTreeMenuItemFrame(treeMenuItem.getTitle(), new UnDockedFrameCloseHook(), treeMenuItem.getView(), null);
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow]"));
	panel.add(treeMenuItem.getView());
	undockedItem.add(panel);
	final String DOCK_FRAME = "Dock frame action";
	panel.getActionMap().put(DOCK_FRAME, createDockFrameAction(undockedItem));
	panel.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), DOCK_FRAME);
	undockedItem.setPreferredSize(new Dimension(950, 760));
	return undockedItem;
    }

    private Action createDockFrameAction(final UndockTreeMenuItemFrame undockedItem) {
	return new AbstractAction() {

	    private static final long serialVersionUID = 62979467973486404L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		activateItem(undockedItem.getView().getAssociatedTreeMenuItem(), true);
	    }

	};
    }

    private class UnDockedFrameCloseHook implements ICloseHook<UndockTreeMenuItemFrame> {

	@Override
	public void closed(final UndockTreeMenuItemFrame frame) {
	    undockedFrames.remove(frame);
	    if (frame.getView().getAssociatedTreeMenuItem().getState() != TreeMenuItemState.DOCK) {
		frame.getView().getAssociatedTreeMenuItem().setState(TreeMenuItemState.ALL);
	    }
	}

    }

    @SuppressWarnings("unchecked")
    @Override
    public void selectItemWithView(final BaseNotifPanel view) {
	if (view.getAssociatedTreeMenuItem().getState() == TreeMenuItemState.DOCK) {
	    super.selectItemWithView(view);
	} else if (view.getAssociatedTreeMenuItem().getState() == TreeMenuItemState.UNDOCK) {
	    for (final UndockTreeMenuItemFrame frame : undockedFrames) {
		if (view == frame.getView()) {
		    frame.setVisible(true);
		    return;
		}
	    }
	}
    }

    /**
     * Returns the progress layer for this tree.
     *
     * @return
     */
    public BlockingIndefiniteProgressLayer getTreeProgressLayer() {
	return treeProgressLayer;
    }

    /**
     * {@link MouseListener} that handles {@link JPopupMenu} triggering, also selects {@link TreeMenuItem} where pop up menu was triggered.
     *
     * @author oleh
     *
     */
    private class PopupMenuListener extends MouseAdapter {

	@Override
	public void mousePressed(final MouseEvent e) {
	    showMenu(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	    showMenu(e);
	}

	/**
	 * Selects the row where pop up menu was triggered and shows it at appropriate position.
	 *
	 * @param e
	 */
	private void showMenu(final MouseEvent e) {
	    if (e.isPopupTrigger() && isEnabled()) {

		final int row = getRowForLocation(e.getX(), e.getY());

		if (row >= 0 && row < getRowCount()) {
		    setSelectionRow(row);
		    final TreeMenuItem<?> treeMenuItem = getSelectedItem();
		    if (treeMenuItem == null) {
			return;
		    }
		    if (isMenuItemVisible(treeMenuItem)) {
			visibilityItem.setState(true);
		    } else {
			visibilityItem.setState(false);
		    }
		    if (!(treeMenuItem instanceof MiSaveAsConfiguration) && !(treeMenuItem instanceof TreeMenuItemWrapper)) {
			visibilityItem.setEnabled(true);
		    } else {
			visibilityItem.setEnabled(false);
		    }
		    switch (treeMenuItem.getState()) {
		    case NONE:
			dockTreeItemAction.setEnabled(false);
			undockTreeItemAction.setEnabled(false);
			break;
		    case DOCK:
			dockTreeItemAction.setEnabled(false);
			undockTreeItemAction.setEnabled(true);
			break;
		    case UNDOCK:
			dockTreeItemAction.setEnabled(true);
			undockTreeItemAction.setEnabled(false);
			break;
		    case ALL:
			dockTreeItemAction.setEnabled(true);
			undockTreeItemAction.setEnabled(true);
		    }
		    popupMenu.show(UndockableTreeMenuWithTabs.this, e.getX(), e.getY());
		}
	    }
	}

    }

    private boolean isMenuItemVisible(final TreeMenuItem<?> treeMenuItem) {
	TreeMenuItem<?> parentMenuItem = treeMenuItem;
	while (parentMenuItem != null) {
	    if (!parentMenuItem.isVisible()) {
		return false;
	    }
	    parentMenuItem = (TreeMenuItem) parentMenuItem.getParent();
	}
	return true;
    }

    /////////////////////////////////////////////////
    //Tracing tree and setting visibility property.//
    /////////////////////////////////////////////////

    private ActionListener createInvisibilityActionListener() {
	return new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		new BlockingLayerCommand<Void>("Save", treeProgressLayer) {

		    private static final long serialVersionUID = 2697125764101171435L;

		    @Override
		    protected Void action(final ActionEvent e) throws Exception {
			if (visibilityItem.getState()) {
			    setMenuItemVisible(getSelectedItem(), true);
			    setChildrenVisible(getSelectedItem(), true);
			    setParentListVisible(getSelectedItem(), true);
			} else {
			    setMenuItemVisible(getSelectedItem(), false);
			    setChildrenVisible(getSelectedItem(), false);
			    updateParentInvisibility(getSelectedItem());
			}
			return null;
		    }

		    @Override
		    protected void postAction(final Void value) {
			super.postAction(value);
			repaint();
		    }

		}.actionPerformed(null);

	    }
	};
    }

    private void setChildrenVisible(final TreeMenuItem<?> menuItem, final boolean visible) {
	if ((menuItem instanceof MiSaveAsConfiguration) || (menuItem instanceof TreeMenuItemWrapper)) {
	    return;
	}
	if (menuItem.getChildCount() > 0) {
	    for (final Enumeration<?> childrenEnum = menuItem.children(); childrenEnum.hasMoreElements();) {
		final TreeMenuItem<?> n = (TreeMenuItem<?>) childrenEnum.nextElement();
		final TreePath path = new TreePath(n.getPath());
		traceTree(path, visible);
	    }
	}
    }

    private void updateParentInvisibility(final TreeMenuItem<?> menuItem) {
	if (!isSiblingVisible(menuItem)) {
	    final TreeMenuItem<?> parentItem = (TreeMenuItem<?>) menuItem.getParent();
	    parentItem.setVisible(false);
	    updateParentInvisibility(parentItem);
	}
    }

    private boolean isSiblingVisible(final TreeMenuItem<?> menuItem) {
	final TreeMenuItem parentItem = (TreeMenuItem) menuItem.getParent();
	if (parentItem.getChildCount() > 0) {
	    for (final Enumeration<?> childrenEnum = parentItem.children(); childrenEnum.hasMoreElements();) {
		final TreeMenuItem<?> n = (TreeMenuItem<?>) childrenEnum.nextElement();
		if (n.isVisible()) {
		    return true;
		}
	    }
	}
	return false;
    }

    private void setParentListVisible(final TreeMenuItem<?> menuItem, final boolean visible) {
	TreeMenuItem<?> parentItem = (TreeMenuItem<?>) menuItem.getParent();
	while (parentItem != null) {
	    if (!(parentItem instanceof MiSaveAsConfiguration) && !(parentItem instanceof TreeMenuItemWrapper)) {
		parentItem.setVisible(visible);
	    }
	    parentItem = (TreeMenuItem<?>) parentItem.getParent();
	}
    }

    private void setMenuItemVisible(final TreeMenuItem<?> menuItem, final boolean visible) {
	if ((menuItem instanceof MiSaveAsConfiguration) || (menuItem instanceof TreeMenuItemWrapper)) {
	    return;
	}
	menuItem.setVisible(visible);
    }

    private void traceTree(final TreePath treePath, final boolean visible) {
	final Object lastPathComponent = treePath.getLastPathComponent();
	if ((lastPathComponent instanceof MiSaveAsConfiguration) || (lastPathComponent instanceof TreeMenuItemWrapper)) {
	    return;
	}
	final TreeMenuItem<?> node = (TreeMenuItem) treePath.getLastPathComponent();
	node.setVisible(visible);
	if (node.getChildCount() > 0) {
	    for (final Enumeration<?> childrenEnum = node.children(); childrenEnum.hasMoreElements();) {
		final TreeNode n = (TreeNode) childrenEnum.nextElement();
		final TreePath path = treePath.pathByAddingChild(n);
		traceTree(path, visible);
	    }
	}
    }

}
