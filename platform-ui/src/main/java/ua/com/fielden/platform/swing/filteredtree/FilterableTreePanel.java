package ua.com.fielden.platform.swing.filteredtree;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.menu.filter.ui.FilterControl;
import ua.com.fielden.platform.swing.treetable.ViewportChangeEvent;

/**
 * {@link JPanel} that holds {@link FilterableTree} and {@link FilterControl} to filter the tree.
 * 
 * @author oleh
 * 
 */
public class FilterableTreePanel extends JPanel {

    private static final long serialVersionUID = 8055296748744451549L;

    private final FilterableTree filterableTree;

    private final FilterControl filterControl;

    /**
     * Creates new {@link FilterableTreePanel} with default width equals to 150 points and default string for filter control equal to "find menu...".
     * 
     * @param filterableTree
     */
    public FilterableTreePanel(final FilterableTree filterableTree) {
	this(filterableTree, "find node...");
    }

    /**
     * Creates new {@link FilterableTreePanel} with default width equals to 150 points and specified string for filter control.
     * 
     * @param filterableTree
     * 
     * @param filterString
     *            TODO
     */
    public FilterableTreePanel(final FilterableTree filterableTree, final String filterString) {
	this(filterableTree, filterString, 150);
    }

    /**
     * Creates new {@link FilterableTreePanel} with specified width and string for filter control.
     * 
     * @param filterableTree
     * @param filterString
     *            TODO
     * @param minWidth
     *            - the specified width of the {@link FilterableTreePanel}
     */
    public FilterableTreePanel(final FilterableTree filterableTree, final String filterString, final int minWidth) {
	super(new MigLayout("fill, insets 0", "[" + (minWidth > 0 ? minWidth : 150) + "::,grow,fill]", "[:30:][grow,fill]"));

	this.filterableTree = filterableTree;

	setBorder(new LineBorder(new Color(140, 140, 140)));
	setBackground(Color.white);

	// ///////////////////////////////////////////////////
	// create the search panel with filter control and //
	// ///////////////////////////////////////////////////
	final JPanel search = new JPanel(new MigLayout("fill, insets 2 2 2 2", "[fill,grow]1[]1[]1[]", "[fill,grow]"));
	search.setBackground(Color.white);
	final IFilterableModel filterableModel = getTree().getModel();
	final JTextField filterTextControl = new JTextField();
	final FilterControl filterControl = new FilterControl(filterTextControl, filterableModel, filterString);
	search.add(filterControl, "grow");
	// create ENTER key stroke and a corresponding action for selecting the first matched menu item in the tree
	final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); // no modifiers
	final String SELECT_MENU_ITEM = "SELECT_MENU_ITEM";
	filterTextControl.getActionMap().put(SELECT_MENU_ITEM, createSelectItemAction(getTree(), filterableModel));
	filterTextControl.getInputMap().put(enter, SELECT_MENU_ITEM);
	// create ESC key stroke for clearing filter
	final KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // no modifiers
	final String CLEAR_FILTER = "CLEAR_FILTER";
	filterTextControl.getActionMap().put(CLEAR_FILTER, createClearFilterAction(filterControl));
	filterTextControl.getInputMap().put(esc, CLEAR_FILTER);

	// /////////////////////////////////////////////////////////////////////////////////////////
	// create action buttons to expand, collapse and collapse all except selected menu items //
	// /////////////////////////////////////////////////////////////////////////////////////////
	final JButton jbExpandAll = new JButton(filterableTree.getExpandAllAction());
	jbExpandAll.setFocusable(false);
	jbExpandAll.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseEntered(final MouseEvent e) {
		jbExpandAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }

	    @Override
	    public void mouseExited(final MouseEvent e) {
		jbExpandAll.setCursor(Cursor.getDefaultCursor());
	    }
	});
	search.add(jbExpandAll, "width 16:16:16, height 16:16:16");

	final JButton jbCollapseAll = new JButton(filterableTree.getCollapseAllAction());
	jbCollapseAll.setFocusable(false);
	jbCollapseAll.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseEntered(final MouseEvent e) {
		jbCollapseAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }

	    @Override
	    public void mouseExited(final MouseEvent e) {
		jbCollapseAll.setCursor(Cursor.getDefaultCursor());
	    }
	});
	search.add(jbCollapseAll, "width 16:16:16, height 16:16:16");

	final JButton jbCollapseAllExceptSelected = new JButton(filterableTree.getCollapseAllExceptSelectedAction());
	jbCollapseAllExceptSelected.setFocusable(false);
	jbCollapseAllExceptSelected.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseEntered(final MouseEvent e) {
		jbCollapseAllExceptSelected.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }

	    @Override
	    public void mouseExited(final MouseEvent e) {
		jbCollapseAllExceptSelected.setCursor(Cursor.getDefaultCursor());
	    }
	});
	search.add(jbCollapseAllExceptSelected, "width 16:16:16, height 16:16:16");

	add(search, "wrap");
	final JScrollPane scroll = new JScrollPane(filterableTree);
	scroll.setBackground(Color.white);
	scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
	scroll.getViewport().addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(final ChangeEvent e) {
		final ViewportChangeEvent event = new ViewportChangeEvent(scroll, scroll.getViewport().getViewPosition(), //
			scroll.getViewport().getExtentSize(), scroll.getSize());
		filterableTree.viewPortChanged(event);
	    }
	});
	add(scroll);

	this.filterControl = filterControl;
    }

    /**
     * Returns the {@link FilterableTree} that this panel holds
     * 
     * @return
     */
    public FilterableTree getTree() {
	return filterableTree;
    }

    /**
     * Create an action for selecting the first matched menu item.
     * 
     * @return
     */
    private Action createSelectItemAction(final FilterableTree treeMenu, final IFilterableModel filterableModel) {
	return new AbstractAction() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// iterate through all visible rows in the tree and find the first matching by filter node
		// once found select it and focus the tree
		for (int index = 0; index < treeMenu.getRowCount(); index++) {
		    final TreeNode currNode = (TreeNode) treeMenu.getPathForRow(index).getLastPathComponent();
		    if (currNode != null && filterableModel.matches(currNode)) {
			treeMenu.setSelectionRow(index);
			treeMenu.requestFocusInWindow();
			break;
		    }
		}

	    }
	};
    }

    /**
     * Creates clear filter action.
     * 
     * @param filerControl
     * @return
     */
    private Action createClearFilterAction(final FilterControl filerControl) {
	return new AbstractAction() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		filerControl.clear();
	    }
	};
    }

    /**
     * Returns the {@link FilterControl} this panel holds
     * 
     * @return
     */
    public FilterControl getFilterControl() {
	return filterControl;
    }

}
