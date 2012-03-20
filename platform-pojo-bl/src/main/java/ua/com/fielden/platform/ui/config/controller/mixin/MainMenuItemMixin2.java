package ua.com.fielden.platform.ui.config.controller.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController2;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


/**
 * A mixin to avoid code duplication for providing RAO and DAP implementations of {@link IMainMenuItemController2}.
 *
 * @author TG Team
 *
 */
public final class MainMenuItemMixin2 {

    private final IMainMenuItemController2 mmiController;
    private final IEntityCentreConfigController2 eccController;
    private User user; // should be set upon user successful login.

    @Inject
    public MainMenuItemMixin2(final IMainMenuItemController2 mmiController, final IEntityCentreConfigController2 eccController) {
	this.mmiController = mmiController;
	this.eccController = eccController;
    }

    /**
     * Retrieves all main menu items (without respective configurations) for the currently logged in user and structures a corresponding hierarchical structure. <br>
     * Please note that the main menu consists of instances representing principal items as well as the "save as" items, which are derived dynamically.
     *
     * @return
     */
    public final List<MainMenuItem> loadMenuSkeletonStructure() {
	if (user == null) {
	    throw new IllegalStateException("Logged in user has not been set.");
	}

	final List<MainMenuItem> allItems = findPrincipalMenuItems();
	allItems.addAll(findSaveAsMenuItems());

	// once there is a linear list of menu items need to build a hierarchical structure
	return buildMenuHierarchy(allItems);
    }

    /**
     * Obtains a linear list of all menu items stored in the cloud.
     * Obtains the list of invisible for the current user items.
     * Sets visibility of the items in the linear list based on this result, which depends on whether user is a base user or otherwise.
     * In case of a non-base user, invisible menu items are removed from the resultant list returned by this method.
     *
     * @return -- a liner list of relevant for the current user menu items with property set visibility.
     */
    public final List<MainMenuItem> findPrincipalMenuItems() {
	// identify user ID to be used for menu information retrieval
	final Long ownerId = user.isBase() ? user.getId() : user.getBasedOnUser().getId();

	// get all items
	final EntityResultQueryModel<MainMenuItem> model = select(MainMenuItem.class).model();
	final OrderingModel orderBy = orderBy().prop("order").asc().model();
	final List<MainMenuItem> allItemsAsLinearList = mmiController.getAllEntities(from(model).with(new fetchAll<MainMenuItem>(MainMenuItem.class)).with(orderBy).build());

	// get those that are marked as invisible
	final EntityResultQueryModel<MainMenuItem> invisibleItemsModel = select(MainMenuItem.class).as("mm").join(MainMenuItemInvisibility.class).as("viz").on() //
	.prop("mm").eq().prop("viz.menuItem")//
	.where().prop("viz.owner").eq().val(ownerId).model();
	final OrderingModel invisibleItemsOrderBy = orderBy().prop("mm.order").asc().model();
	final List<MainMenuItem> invisibleItems = mmiController.getAllEntities(from(invisibleItemsModel).with(new fetchAll<MainMenuItem>(MainMenuItem.class)).with(invisibleItemsOrderBy).build()); // could be optimized by not fetching all

	if (user.isBase()) { // set visibility property for menu items, which does not take into account hierarchical structure
	    for (final MainMenuItem item : allItemsAsLinearList) {
		item.setVisible(!invisibleItems.contains(item));
	    }
	} else { // set visibility property for menu items and remove invisible ones
	    for (final Iterator<MainMenuItem> iter = allItemsAsLinearList.iterator(); iter.hasNext();) {
		final MainMenuItem item = iter.next();
		if (invisibleItems.contains(item)) {
		    iter.remove();
		} else {
		    item.setVisible(true); // not really required, because the default value is true, but for clarity it is nice to set it
		}
	    }
	}

	return allItemsAsLinearList;
    }

    /**
     * Returns a linear list of non-persistent menu items created based on entity centre configurations that are related to the currently logged in user.
     * <p>
     * IMPORTANT: visibility for created items is not set here as it can only be determined from the state of the parental menu item. The necessary processing should happen upon
     * construction of the menu hierarchy.
     *
     * @return
     */
    public final List<MainMenuItem> findSaveAsMenuItems() {
	// identify whether what IDs should be used for configuration retrieval
	final Long[] ids = user.isBase() ? new Long[] { user.getId() } : new Long[] { user.getId(), user.getBasedOnUser().getId() };

	// retrieve entity centre configurations, which do not correspond to principle menu items and belong to the users identified by ids
	final EntityResultQueryModel<EntityCentreConfig> model = select(EntityCentreConfig.class).where()//
	.prop("owner").in().values(ids)//
	.and().prop("principal").eq().val(false).model(); // TODO may be it is better to order by title

	final OrderingModel orderBy = orderBy().prop("menuItem.order").asc().model();

	final List<EntityCentreConfig> eccs = eccController.getAllEntities(from(model).with(orderBy).build());

	// iterate through the list of configurations and make new non-persistent instances of type MainMenuItem
	// representing non-principal (i.e. save as) menu items of the same type as the referenced by the configuration menu item.
	//
	// this referenced menu item also serves as a parent for created "save as" menu items.
	final List<MainMenuItem> saveAsItems = new ArrayList<MainMenuItem>();
	for (final EntityCentreConfig config : eccs) {
	    final MainMenuItem item = config.getEntityFactory().newEntity(MainMenuItem.class);
	    item.setVisible(true); // correct visibility cannot be determined here
	    item.setKey(config.getMenuItem().getKey());
	    item.setTitle(config.getTitle());
	    item.setParent(config.getMenuItem());
	    item.setPrincipal(false);
	    item.setOrder(config.getMenuItem().getOrder());
	    item.setConfig(config);
	    saveAsItems.add(item);
	}

	return saveAsItems;
    }

    /**
     * Builds menu hierarchy out of the list of all menu items.
     *
     * @param linearItems
     * @return
     */
    public final List<MainMenuItem> buildMenuHierarchy(final List<MainMenuItem> items) {
	final List<MainMenuItem> linearItems = new ArrayList<MainMenuItem>(items); // make a copy in order not to change the original items list
	// initialisation -- get the root (topmost) level of items for processing, which can only be the principal items
	final List<MainMenuItem> rootLevel = new ArrayList<MainMenuItem>();
	for (final Iterator<MainMenuItem> iter = linearItems.iterator(); iter.hasNext();) {
	    final MainMenuItem item = iter.next();
	    if (item.getParent() == null) { // this is the definition of the root item
		if (user.isBase() || item.isVisible()) {
		    rootLevel.add(item);
		}
		iter.remove();
	    }
	}

	// build hierarchy by building tree levels in descending order -- from top to bottom
	List<MainMenuItem> currRootLevel = new ArrayList<MainMenuItem>(rootLevel);
	boolean treeWasUdated = true;
	while (!currRootLevel.isEmpty() && !linearItems.isEmpty() && treeWasUdated) {
	    treeWasUdated = false; // let's be pessimistic
	    final List<MainMenuItem> processedLeafItems = new ArrayList<MainMenuItem>(); // these items will become the current root on the next iteration

	    for (final MainMenuItem rootItem : currRootLevel) {
		for (final Iterator<MainMenuItem> iter = linearItems.iterator(); iter.hasNext();) {
		    final MainMenuItem item = iter.next();
		    if (rootItem.equals(item.getParent())) {
			// item should be visible only if it was visible and its parent is visible
			item.setVisible(rootItem.isVisible() && item.isVisible());

			if (user.isBase()) { // in case of base users need to retain all menu items, but set visibility correctly
			    treeWasUdated = true;
			    rootItem.addChild(item);
			    processedLeafItems.add(item);
			} else if (item.isPrincipal() && item.isVisible() || !item.isPrincipal() && rootItem.isVisible()) { // need to include only visible menu items for a non-base user
			    treeWasUdated = true;
			    item.setVisible(true);
			    rootItem.addChild(item);
			    processedLeafItems.add(item);
			}

			iter.remove();
		    }
		}
	    }

	    currRootLevel = processedLeafItems;
	}
	// return the topmost root level
	return rootLevel;
    }

    public User getUser() {
	return user;
    }

    public void setUser(final User user) {
	this.user = user;
    }

}
