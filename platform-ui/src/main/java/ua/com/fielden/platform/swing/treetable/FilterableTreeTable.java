package ua.com.fielden.platform.swing.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree.DelegatingRenderer;
import org.jdesktop.swingx.JXTreeTable;

import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.utils.ResourceLoader;

public class FilterableTreeTable extends JXTreeTable {

    private static final long serialVersionUID = -5695340847119017852L;

    private final FilterableTreeTableModel filterableModel;

    private final Action expandAllAction = createExpandAllAction();
    private final Action collapseAllAction = createCollapseAllAction();
    private final Action collapseAllExceptSelectedAction = createCollapseAllExceptSelectedAction();

    /**
     * Holds current viewport's position.
     */
    private Point viewportPosition = new Point(0, 0);
    /**
     * Holds current view's size.
     */
    private Dimension viewportSize = new Dimension(0, 0);

    public FilterableTreeTable(final FilterableTreeTableModel treeTableModel, final boolean rootVisible) {
	super(treeTableModel);
	setRootVisible(rootVisible);
	this.filterableModel = treeTableModel;

	filterableModel.addFilterListener(new IFilterListener() {

	    @Override
	    public void postFilter(final IFilterableModel model) {
		expandAll();
	    }

	    @Override
	    public boolean nodeVisibilityChanged(final TreeNode treeNode, final boolean prevValue, final boolean newValue) {
		return false;
	    }

	    @Override
	    public void preFilter(final IFilterableModel model) {
	    }
	});

	enhanceRenderer(filterableModel);

	setDefaultRenderer(Boolean.class, new TableCellRenderer() {

	    private final TableCellRenderer stringCellRenderer = new DefaultTableCellRenderer(),//
	    booleanCellRenderer = getDefaultRenderer(Boolean.class);

	    @Override
	    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final Component stringComponent = stringCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final Component boolComponent = booleanCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final Color backgroundColor = stringComponent.getBackground();
		boolComponent.setBackground(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()));
		return boolComponent;
	    }

	});
    }

    // defines the renderer if the delegating renderer is not of the DefaultTreeCellRenderer type
    private void enhanceRenderer(final IFilterableModel model) {
	setClosedIcon(null);
	setOpenIcon(null);
	setLeafIcon(null);
	setTreeCellRenderer(new TreeCellRenderer() {

	    private final TreeCellRenderer defaultRenderer = ((DelegatingRenderer) getTreeCellRenderer()).getDelegateRenderer();

	    @Override
	    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		final Component defaultCellComponent = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		final Font originFont = defaultCellComponent.getFont().deriveFont(Font.PLAIN);
		final Font derivedFont = originFont.deriveFont(Font.BOLD);
		if (model.matches((TreeNode) value)) {
		    defaultCellComponent.setFont(derivedFont);
		} else {
		    defaultCellComponent.setFont(originFont);
		}
		return defaultCellComponent;
	    }

	});
    }

    @Override
    public void tableChanged(final TableModelEvent e) {
	if ((isStructureChanged(e) || isUpdate(e)) && (filterableModel != null) && !(filterableModel.isReloading())) {
	    super.tableChanged(e);
	} else {
	    resizeAndRepaint();
	}

    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
	if (!e.getValueIsAdjusting()) {
	    final int firstIndex = Math.min(getRowCount() - 1, Math.max(e.getFirstIndex(), 0));
	    final int lastIndex = Math.min(getRowCount() - 1, Math.max(e.getLastIndex(), 0));
	    final Rectangle firstRowRect = getCellRect(firstIndex, 0, false);
	    final Rectangle lastRowRect = getCellRect(lastIndex, getColumnCount() - 1, false);
	    final Rectangle dirtyRegion = firstRowRect.union(lastRowRect);
	    repaint(dirtyRegion);
	} else {
	    super.valueChanged(e);
	}
    }

    /**
     * Collapses all rows except the one that is selected
     */
    public void collapseAllExceptSelected() {
	for (int rowCounter = getRowCount(); rowCounter >= 0; rowCounter--) {
	    final TreePath path = getPathForRow(rowCounter);
	    final TreePath selectedPath = getPathForRow(getSelectedRow());
	    if (path != null && selectedPath != null) {
		if (!path.isDescendant(selectedPath) || selectedPath.isDescendant(path)) {
		    collapseRow(rowCounter);
		}
	    }
	}
    }

    /**
     * Returns the action that expands all tree table nodes
     * 
     * @return
     */
    private Action createExpandAllAction() {
	final Action action = new Command<Void>("") {

	    private static final long serialVersionUID = 344489827335865704L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		expandAll();
	    }

	};

	final Icon icon = ResourceLoader.getIcon("images/plus.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Expand all menu items");
	return action;
    }

    /**
     * Returns the action that collapses all tree table nodes
     * 
     * @return
     */
    private Action createCollapseAllAction() {
	final Action action = new Command<Void>("") {
	    private static final long serialVersionUID = 344489827335865704L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		collapseAll();
	    }

	};

	final Icon icon = ResourceLoader.getIcon("images/minus.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Collaps all menu items");
	return action;
    }

    /**
     * Creates and returns the action that collapses all tree table nodes except the one that is currently selected.
     * 
     * @return
     */
    private Action createCollapseAllExceptSelectedAction() {
	final Action action = new Command<Void>("") {
	    private static final long serialVersionUID = 344489827335865704L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		collapseAllExceptSelected();
	    }

	};

	final Icon icon = ResourceLoader.getIcon("images/tree.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Collapse all except selected menu item");
	return action;
    }

    /**
     * see {@link #createExpandAllAction()}
     * 
     * @return
     */
    public Action getExpandAllAction() {
	return expandAllAction;
    }

    /**
     * see {@link #createCollapseAllAction()}
     * 
     * @return
     */
    public Action getCollapseAllAction() {
	return collapseAllAction;
    }

    /**
     * see {@link #createCollapseAllExceptSelectedAction()}
     * 
     * @return
     */
    public Action getCollapseAllExceptSelectedAction() {
	return collapseAllExceptSelectedAction;
    }

    ///////////////////////Incremental expanding.///////////////////////
    /**
     * Expands only nodes visible in the viewport.
     */
    @Override
    public void expandAll() {
	final List<TreePath> expandPaths = new ArrayList<TreePath>();
	TreePath startPath=getPathForLocation(viewportPosition.x, viewportPosition.y);
	startPath = startPath == null ? getPathForLocation(0, 0) : startPath;
	if(startPath!=null){
	    collectPathsToExpand(startPath, expandPaths, getViewportRowCount());
	    for(final TreePath treePath:expandPaths){
		expandPath(treePath);
	    }
	}
    }

    /**
     * Returns list of <code>numOfPaths</code> paths those starts from specified startPath. Uses depth-first search to build the paths.<br>
     * Throws NullPointerException if startPath is null.
     * 
     * @param startPath
     * @param pathsToExpand
     * @param numOfPaths
     * @return
     */
    private boolean collectPathsToExpand(final TreePath startPath,final List<TreePath> pathsToExpand, final int numOfPaths){
	if (pathsToExpand.size() >= numOfPaths) {
	    return true;
	} else {
	    pathsToExpand.add(startPath);
	    final Object child = getTreeTableModel().getChild(startPath.getLastPathComponent(), 0);
	    if(child==null){
		final TreePath availablePath=getNextAvailablePath(startPath);
		return availablePath==null?false:collectPathsToExpand(availablePath, pathsToExpand, numOfPaths);
	    }else{
		return collectPathsToExpand(startPath.pathByAddingChild(child), pathsToExpand, numOfPaths);
	    }
	}
    }

    /**
     * Returns next available {@link TreePath} for specified one. This method uses back tracking to identify available path.
     * 
     * @param startPath
     * @return
     */
    private TreePath getNextAvailablePath(final TreePath startPath) {
	TreePath path=startPath;
	while(path.getParentPath()!=null){
	    final TreePath parentPath=path.getParentPath();
	    final Object lastComponent= path.getLastPathComponent();
	    final int childIndex = getTreeTableModel().getIndexOfChild(parentPath.getLastPathComponent(), lastComponent);
	    final Object child=getTreeTableModel().getChild(parentPath.getLastPathComponent(), childIndex+1);
	    if(child==null){
		path=parentPath;
	    }else{
		return parentPath.pathByAddingChild(child);
	    }
	}
	return null;
    }

    /**
     * Returns the number of rows visible in the viewport.
     * 
     * @return
     */
    private int getViewportRowCount(){
	return (int)Math.floor(viewportSize.getHeight()/getRowHeight());
    }


    /**
     * Override in order to handle viewport change events. This method will be triggered when scroll pane changes it's viewport position or size.
     */
    protected void viewPortChanged(final ViewportChangeEvent e) {
	this.viewportPosition=e.getViewportPosition();
	this.viewportSize = e.getSize();
    }



}
