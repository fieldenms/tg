package ua.com.fielden.platform.ui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

/**
 * A type for persisting an individual main menu item. Existence of an instance of this type simply indicates the existence of a corresponding main menu item.
 * <p>
 * Property <code>key</code> should contain a string representation of a corresponding menu item class. The Class type could not be used as a key type because it is not Comparable.
 * <p>
 * Property <code>parent</code> should be used for specifying parent/child relationships between menu items in order to build menu hierarchy.
 * <p>
 * Menu items cannot be deleted as the result of a user action. Menu items are defined by the system integrators most likely as part of the deployment.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Menu item type")
@DescTitle("Description")
@CompanionObject(IMainMenuItemController.class)
@MapEntityTo("MAIN_MENU")
public class MainMenuItem extends AbstractEntity<String> implements ITreeNode<MainMenuItem> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo("ID_PARENT")
    private MainMenuItem parent;
    @IsProperty
    @MapTo("ITEM_ORDER")
    private int order;
    @IsProperty
    @MapTo("TITLE")
    private String title;
    /** This is a non-persistent property, which gets calculated at runtime by the application to indicate item's visibility. */
    private boolean visible = true;
    /** This is a non-persistent property, which gets calculated at runtime by the application to indicate whether item is principal (i.e. not a save as item). */
    private boolean principal = true;
    /** This is a non-persistent property, which gets calculated at runtime by the application to hold an instance of a corresponding entity centre configuration. */
    private EntityCentreConfig config;
    /** A calculated field (not a property per se) populated at some stage during main menu construction to hold references to sub-menu items */
    private final List<MainMenuItem> children = new ArrayList<MainMenuItem>();

    /**
     * A helper setter to convert menu item type to the string key value.
     *
     * @param menuItemType
     */
    public MainMenuItem setMenuItemType(final Class<?> menuItemType) {
	setKey(PropertyTypeDeterminator.stripIfNeeded(menuItemType).getName());
	return this;
    }

    /**
     * A helper getter to obtain menu item type from key's string value.
     *
     * @return
     */
    public Class<?> getMenuItemType() {
	try {
	    return Class.forName(getKey());
	} catch (final ClassNotFoundException e) {
	    throw new IllegalStateException("Menu item key '" + getKey() + "' is not a valid class name.");
	}
    }

    public MainMenuItem getParent() {
	return parent;
    }

    @Observable
    @EntityExists(MainMenuItem.class)
    public MainMenuItem setParent(final MainMenuItem parent) {
	this.parent = parent;
	return this;
    }

    public List<MainMenuItem> getChildren() {
	return Collections.unmodifiableList(children);
    }

    public MainMenuItem addChild(final MainMenuItem child) {
	if (child.getParent() != null && !child.getParent().equals(this)) {
	    throw new Result(this, new IllegalArgumentException("Menu item " + child + " already has a parent."));
	}
	if (!containsChild(child)) {
	    children.add(child);
	    // need to enforce resetting of the parent even in case of the same value (i.e. equals) to have the correct reference
	    child.setInitialising(true);
	    child.setParent(this);
	    child.setInitialising(false);
	}
	return this;
    }

    public void removeChild(final MainMenuItem item) {
	for (final Iterator<MainMenuItem> iter = children.iterator(); iter.hasNext();) {
	    final MainMenuItem child = iter.next();
	    if (child.getKey().equals(item.getKey()) && child.getTitle().equals(item.getTitle())) {
		iter.remove();
		return;
	    }
	}
    }

    private boolean containsChild(final MainMenuItem item) {
	for (final MainMenuItem child : children) {
	    if (child.getKey().equals(item.getKey()) && child.getTitle().equals(item.getTitle())) {
		return true;
	    }
	}
	return false;
    }

    public int getOrder() {
	return order;
    }

    @Observable
    public MainMenuItem setOrder(final int order) {
	this.order = order;
	return this;
    }

    public boolean isVisible() {
	return visible;
    }

    public MainMenuItem setVisible(final boolean visible) {
	this.visible = visible;
	return this;
    }

    public boolean isPrincipal() {
	return principal;
    }

    public MainMenuItem setPrincipal(final boolean principal) {
	this.principal = principal;
	return this;
    }

    public String getTitle() {
	return title;
    }

    @Observable
    public MainMenuItem setTitle(final String title) {
	this.title = title;
	return this;
    }

    public EntityCentreConfig getConfig() {
	return config;
    }

    public MainMenuItem setConfig(final EntityCentreConfig config) {
	this.config = config;
	return this;
    }

    @Override
    public List<? extends ITreeNode<MainMenuItem>> daughters() {
	return Collections.unmodifiableList(children);
    }

    @Override
    public MainMenuItem state() {
	return this;
    }
}
