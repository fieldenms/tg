package ua.com.fielden.platform.swing.menu;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * A tree-based menu item. Sub-menu items can be added by using method <code>addItem</code>, which is a convenient wrapper around {@link Vector#add(Object)}.
 * <p>
 * The provided view should become visible upon menu item selection. This behaviour should be controlled by a tree selection listener associated with a tree representing a menu.
 * <p>
 * The <code>toString</code> method of the provided view is used as a menu item title. Thus, one should take care by overriding view's <code>toString</code>.
 * 
 * @author TG Team
 */
public class TreeMenuItem<V extends BaseNotifPanel> extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 1L;

    private final V view;
    private final JPanel infoPanel;
    /**
     * Indicates wither this just a group item. Group items do not get activated and are usually used purely for representing a header for a groupe of other menu items
     */
    private final boolean groupItem;
    private final String title;

    /**
     * Determines current tree menut item state (DOCK, UNDOCK, NONE, ALL)
     */
    private TreeMenuItemState state;

    /**
     * Determines whether this menu item is visible or not
     */
    private boolean visible = true;

    /**
     * This is a primary constructor accepting both the view and info panel.
     * 
     * @param view
     * @param infoPanel
     */
    public TreeMenuItem(final V view, final String title, final JPanel infoPanel, final boolean groupItem) {
	this.view = view;
	if (this.view != null) {
	    this.view.setAssociatedTreeMenuItem(this);
	    setState(TreeMenuItemState.ALL);
	} else {
	    setState(TreeMenuItemState.NONE);
	}
	this.infoPanel = infoPanel;
	this.groupItem = groupItem;
	this.title = title;
    }

    /**
     * This is a convenience constructor, which can be used where info panel is not required.
     * 
     * @param view
     */
    public TreeMenuItem(final V view) {
	this(view, view.toString(), new SimpleInfoPanel(view.getInfo()), false);
    }

    /**
     * This is a convenience constructor, which can be used for constructing group items.
     * 
     * @param view
     */
    public TreeMenuItem(final String title, final String info) {
	this(null, title, new SimpleInfoPanel(info), true);
    }

    @Override
    public String toString() {
	return title;
    }

    /**
     * Returns title of this {@link TreeMenuItem}.
     * 
     * @return
     */
    public String getTitle() {
	return title;
    }

    /**
     * A convenient wrapper around vector's add method for appending menu items.
     * 
     * @param item
     * @return
     */
    public TreeMenuItem<V> addItem(final TreeMenuItem<?> item) {
	super.add(item);
	return this;
    }

    public V getView() {
	return view;
    }

    public JPanel getInfoPanel() {
	return infoPanel;
    }

    /**
     * A convenient method advising if an info panel is associated with this menu item.
     * 
     * @return
     */
    public boolean hasInfoPanel() {
	return infoPanel != null;
    }

    public boolean isGroupItem() {
	return groupItem;
    }

    public TreeMenuItemState getState() {
	return state;
    }

    public void setState(final TreeMenuItemState state) {
	this.state = state;
    }

    public boolean isVisible() {
	return visible;
    }

    public void setVisible(final boolean visible) {
	this.visible = visible;
    }

}
