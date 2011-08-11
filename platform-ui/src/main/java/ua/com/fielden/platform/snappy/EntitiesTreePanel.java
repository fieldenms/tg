package ua.com.fielden.platform.snappy;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
import ua.com.fielden.platform.swing.actions.BlockingCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.menu.filter.ui.FilterControl;
import ua.com.fielden.platform.swing.treetable.ViewportChangeEvent;

/**
 * A control conveniently incorporating {@link SnappyEntitiesTree} and the search bar.
 * 
 * @author 01es, Jhou
 * 
 */
public class EntitiesTreePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final SnappyEntitiesTree entitiesTree;
    private final BlockingIndefiniteProgressPane blockingIndefiniteProgressPane;
    private final List<JButton> buttons;

    public EntitiesTreePanel(final SnappyEntitiesTree entitiesTree, final BlockingIndefiniteProgressPane blockingIndefiniteProgressPane) {
	super(new MigLayout("fill, insets 0", "[150::,grow,fill]", "[:30:][grow,fill]"));
	setBorder(new LineBorder(new Color(140, 140, 140)));
	setBackground(Color.white);

	this.entitiesTree = entitiesTree;
	this.blockingIndefiniteProgressPane = blockingIndefiniteProgressPane;

	/////////////////////////////////////////////////////
	// create the search panel with filter control and //
	/////////////////////////////////////////////////////
	final JPanel search = new JPanel(new MigLayout("fill, insets 2 2 2 2", "[fill,grow]1[]1[]1[]", "[fill,grow]"));
	search.setBackground(Color.white);
	final IFilterableModel filterableModel = entitiesTree.getModel();
	final JTextField filterTextControl = new JTextField();
	final FilterControl filterControl = new FilterControl(filterTextControl, filterableModel, "find property/entity...");
	search.add(filterControl, "grow");
	// create ENTER key stroke and a corresponding action for selecting the first matched menu item in the tree
	final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); // no modifiers
	final String SELECT_MENU_ITEM = "SELECT_MENU_ITEM";
	filterTextControl.getActionMap().put(SELECT_MENU_ITEM, createSelectItemAction(entitiesTree, filterableModel));
	filterTextControl.getInputMap().put(enter, SELECT_MENU_ITEM);
	// create ESC key stroke for clearing filter
	final KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // no modifiers
	final String CLEAR_FILTER = "CLEAR_FILTER";
	filterTextControl.getActionMap().put(CLEAR_FILTER, createClearFilterAction(filterControl));
	filterTextControl.getInputMap().put(esc, CLEAR_FILTER);
	entitiesTree.getActionMap().put(CLEAR_FILTER, createClearFilterAction(filterControl));
	entitiesTree.getInputMap().put(esc, CLEAR_FILTER);

	//	////////////////////////////////
	//	// assign navigation hot keys //
	//	////////////////////////////////
	//	// create CTRL+1 key stroke and a corresponding action to bring focus to the tree menu
	//	final KeyStroke activateTreeMenu = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK);
	//	final String ACTIVATE_TREE_MENU = "ACTIVATE_TREE_MENU";
	//	treeMenu.getActionMap().put(ACTIVATE_TREE_MENU, createFocusTreeMenuAction(treeMenu));
	//	treeMenu.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(activateTreeMenu, ACTIVATE_TREE_MENU);
	//	// create CTRL+2 key stroke and a corresponding action to bring focus to the filter control
	//	final KeyStroke activateFilterControl = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK);
	//	final String ACTIVATE_FILTER_CONTROL = "ACTIVATE_FILTER_CONTROL";
	//	filterControl.getActionMap().put(ACTIVATE_FILTER_CONTROL, createFocusFilterControlAction(filterControl));
	//	filterControl.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(activateFilterControl, ACTIVATE_FILTER_CONTROL);
	//	// create CTRL+3 key stroke and a corresponding action to bring focus to the holder panel
	//	final KeyStroke focusHolder = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK);
	//	final String FOCUS_HOLDER = "FOCUS_HOLDER";
	//	filterControl.getActionMap().put(FOCUS_HOLDER, createFocusHolderAction(treeMenu));
	//	filterControl.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(focusHolder, FOCUS_HOLDER);

	buttons = new ArrayList<JButton>();
	///////////////////////////////////////////////////////////////////////////////////////////
	// create action buttons to expand, collapse and collapse all except selected menu items //
	///////////////////////////////////////////////////////////////////////////////////////////
	final JButton jbExpandAll = new JButton(entitiesTree.getExpandAllAction());
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
	buttons.add(jbExpandAll);

	final JButton jbCollapseAll = new JButton(entitiesTree.getCollapseAllAction());
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
	buttons.add(jbCollapseAll);

	final JButton jbCollapseAllExceptSelected = new JButton(entitiesTree.getCollapseAllExceptSelectedAction());
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
	buttons.add(jbCollapseAllExceptSelected);

	add(search, "wrap");
	entitiesTree.collapseAll();

	final JScrollPane scroll = new JScrollPane(entitiesTree);
	scroll.setBackground(Color.white);
	scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
	scroll.getViewport().addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(final ChangeEvent e) {
		final ViewportChangeEvent event = new ViewportChangeEvent(scroll, scroll.getViewport().getViewPosition(), //
			scroll.getViewport().getExtentSize(), scroll.getSize());
		entitiesTree.viewPortChanged(event);
	    }
	});
	add(scroll);
    }

    /**
     * Create an action for selecting the first matched menu item.
     * 
     * @return
     */
    private Action createSelectItemAction(final SnappyEntitiesTree entitiesTree, final IFilterableModel filterableModel) {
	return new AbstractAction() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// iterate through all visible rows in the tree and find the first matching by filter node
		// once found select it and focus the tree
		for (int index = 0; index < entitiesTree.getRowCount(); index++) {
		    final TreeNode currNode = (TreeNode) entitiesTree.getPathForRow(index).getLastPathComponent();
		    if (currNode != null && filterableModel.matches(currNode)) {
			entitiesTree.setSelectionRow(index);
			entitiesTree.requestFocusInWindow();
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
	//	return new AbstractAction() {
	//	    private static final long serialVersionUID = 1L;
	//
	//	    @Override
	//	    public void actionPerformed(final ActionEvent e) {
	//		SwingUtilitiesEx.invokeLater(new Runnable() {
	//		    public void run() {
	//			filerControl.clear();
	//			entitiesTree.bringSelectedIntoView();
	//			entitiesTree.collapseAllExceptSelected();
	//		    }
	//		});
	//
	//	    }
	//	};

	return new BlockingCommand<Void>("filter action clearing...", blockingIndefiniteProgressPane) {
	    private static final long serialVersionUID = -4220416657650488528L;

	    @Override
	    protected boolean preAction() {
		super.preAction();

		return true;
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		filerControl.clear();
		entitiesTree.collapseAllExceptSelected();
		entitiesTree.bringSelectedIntoView();
		super.postAction(value);
	    }

	};
    }

    public SnappyEntitiesTree getEntitiesTree() {
	return entitiesTree;
    }

    public List<JButton> getButtons() {
	return buttons;
    }

}
