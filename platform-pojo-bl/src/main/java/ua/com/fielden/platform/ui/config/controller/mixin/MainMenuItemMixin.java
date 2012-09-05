package ua.com.fielden.platform.ui.config.controller.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


/**
 * A mixin to avoid code duplication for providing RAO and DAO implementations of {@link IMainMenuItemController}.
 *
 * @author TG Team
 *
 */
public final class MainMenuItemMixin {
    private final IMainMenuItemController mmiController;
    private final IEntityCentreConfigController eccController;
    private final IMainMenuItemInvisibilityController mmiiController;
    private final EntityFactory factory;
    private User user; // should be set upon user successful login.
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    public MainMenuItemMixin(final IMainMenuItemController mmiController, final IEntityCentreConfigController eccController, final IMainMenuItemInvisibilityController mmiiController, final EntityFactory factory) {
	this.mmiController = mmiController;
	this.eccController = eccController;
	this.mmiiController = mmiiController;
	this.factory = factory;
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
	final List<MainMenuItem> allItemsAsLinearList = mmiController.getAllEntities(from(model).with(fetchAll(MainMenuItem.class)).with(orderBy).model());

	// get those that are marked as invisible
	final EntityResultQueryModel<MainMenuItem> invisibleItemsModel = select(MainMenuItem.class).as("mm").join(MainMenuItemInvisibility.class).as("viz").on() //
	.prop("mm").eq().prop("viz.menuItem")//
	.where().prop("viz.owner").eq().val(ownerId).model();
	final OrderingModel invisibleItemsOrderBy = orderBy().prop("mm.order").asc().model();
	final List<MainMenuItem> invisibleItems = mmiController.getAllEntities(from(invisibleItemsModel).with(fetchAll(MainMenuItem.class)).with(invisibleItemsOrderBy).model()); // could be optimized by not fetching all

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

	final List<EntityCentreConfig> eccs = eccController.getAllEntities(from(model).with(orderBy).model());

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
	    item.setOrder(config.getMenuItem().getOrder()); // TODO is this really correct??
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

    private static final class EntityCentreConfigKey {
	private final User owner;
	private final String title;
	private final String mainMenuItemKey;

	protected EntityCentreConfigKey(final User owner, final String title, final String mainMenuItemKey) {
	    this.owner = owner;
	    this.title = title;
	    this.mainMenuItemKey = mainMenuItemKey;
	}

	public User getOwner() {
	    return owner;
	}

	public String getTitle() {
	    return title;
	}

	public String getMainMenuItemKey() {
	    return mainMenuItemKey;
	}
    }

    private static final class EntityCentreConfigBody {
	private final boolean principal;
	private final byte[] configBody;

	protected EntityCentreConfigBody(final boolean principal, final byte[] configBody) {
	    this.principal = principal;
	    this.configBody = configBody;
	}

	public boolean isPrincipal() {
	    return principal;
	}

	public byte[] getConfigBody() {
	    return configBody;
	}
    }

    private static final class MainMenuItemInvisibilityKey {
	private final User owner;
	private final String mainMenuItemKey;

	protected MainMenuItemInvisibilityKey(final User owner, final String mainMenuItemKey) {
	    this.owner = owner;
	    this.mainMenuItemKey = mainMenuItemKey;
	}

	public User getOwner() {
	    return owner;
	}

	public String getMainMenuItemKey() {
	    return mainMenuItemKey;
	}
    }

    /**
     * Updates existing persisted menu items with new "development" ones. This method is only applicable for base user, which is capable for menu items modification.
     * <p>
     * It covers correct handling of menu items tree structure including: <br>
     *  1. adding new items; <br>
     *  2. removing old items (<b>IMPORTANT:</b> all tree menu structure will be purged including entity-centres with "saveAsses" and all item invisibility information); <br>
     *  3. modification of old items aka "item type = key" modification (<b>IMPORTANT:</b> in case of <b>PRINCIPLE</b> item modification -- all tree menu structure will be purged including entity-centres with "saveAsses" and all item invisibility information); <br>
     *  4. items order modification. <br>
     *
     * @param developmentMainMenuStructureBuilder
     * @param username
     * @return
     */
    public final List<MainMenuItem> updateMenuItemsWithDevelopmentOnes(final IMainMenuStructureBuilder developmentMainMenuStructureBuilder) {
	if (!user.isBase()) {
	    throw new IllegalArgumentException("Updating of menu items is not permitted for non-base user. Current user [" + user + "] is not base user.");
	}
	final List<MainMenuItem> developmentMainMenuItems = developmentMainMenuStructureBuilder.build();
	final List<MainMenuItem> updatedMainMenuItems = new ArrayList<MainMenuItem>();

	// retrieve all EntityCentreConfig's, locally keep meta-info, and then purge them all
	final EntityResultQueryModel<EntityCentreConfig> modelEcc = select(EntityCentreConfig.class)./*where().prop("owner.key").eq().val(user.getKey()).*/model();
	final List<EntityCentreConfig> eccs = eccController.getAllEntities(from(modelEcc).model());
	final Map<EntityCentreConfigKey, EntityCentreConfigBody> centresKeysAndBodies = new HashMap<EntityCentreConfigKey, EntityCentreConfigBody>();
	for (final EntityCentreConfig ecc : eccs) {
	    centresKeysAndBodies.put(new EntityCentreConfigKey(ecc.getOwner(), ecc.getTitle(), ecc.getMenuItem().getKey()), new EntityCentreConfigBody(ecc.isPrincipal(), ecc.getConfigBody()));
	}
	eccController.delete(modelEcc);
	// retrieve all MainMenuItemInvisibility's, locally keep meta-info, and then purge them all
	final EntityResultQueryModel<MainMenuItemInvisibility> modelMmii = select(MainMenuItemInvisibility.class)./*where().prop("owner.key").eq().val(user.getKey()).*/model();
	final List<MainMenuItemInvisibility> mmiis = mmiiController.getAllEntities(from(modelMmii).model());
	final Set<MainMenuItemInvisibilityKey> invisibilitiesKeys = new HashSet<MainMenuItemInvisibilityKey>();
	for (final MainMenuItemInvisibility mmii : mmiis) {
	    invisibilitiesKeys.add(new MainMenuItemInvisibilityKey(mmii.getOwner(), mmii.getMenuItem().getKey()));
	}
	mmiiController.delete(modelMmii);
	// purge all old menu items
	final List<MainMenuItem> mmis = loadMenuSkeletonStructure();
	for (final MainMenuItem rootItem : mmis) {
	    purgeAll(rootItem);
	}
	// persist new menu items
	for (final MainMenuItem rootDevelopmentMainMenuItem : developmentMainMenuItems) {
	    updatedMainMenuItems.add(saveMenuItem(rootDevelopmentMainMenuItem));
	}
	// persist old EntityCentreConfig's
	for (final Entry<EntityCentreConfigKey, EntityCentreConfigBody> centresKeyAndBody : centresKeysAndBodies.entrySet()) {
	    final MainMenuItem mmi = mmiController.findByKey(centresKeyAndBody.getKey().getMainMenuItemKey());
	    if (mmi != null) {
		final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, centresKeyAndBody.getKey().getOwner(), centresKeyAndBody.getKey().getTitle(), mmi);
		ecc.setPrincipal(centresKeyAndBody.getValue().isPrincipal());
		ecc.setConfigBody(centresKeyAndBody.getValue().getConfigBody());
		eccController.save(ecc);
	    } else {
		logger.warn("The Entity Centre Config for owner [" + centresKeyAndBody.getKey().getOwner() + "] and title " + centresKeyAndBody.getKey().getTitle() + " and item [" + centresKeyAndBody.getKey().getMainMenuItemKey() + "] has been purged due to non-existence of item [" + centresKeyAndBody.getKey().getMainMenuItemKey() + "] after update procedure.");
	    }
	}
	// persist old MainMenuItemInvisibility's
	for (final MainMenuItemInvisibilityKey invisibilityKey : invisibilitiesKeys) {
	    final MainMenuItem mmi = mmiController.findByKey(invisibilityKey.getMainMenuItemKey());
	    if (mmi != null) {
		final MainMenuItemInvisibility mmii = factory.newByKey(MainMenuItemInvisibility.class, invisibilityKey.getOwner(), mmi);
		mmiiController.save(mmii);
	    } else {
		logger.warn("The Main Menu Item Invisibility for owner [" + invisibilityKey.getOwner() + "] and item [" + invisibilityKey.getMainMenuItemKey() + "] has been purged due to non-existence of item [" + invisibilityKey.getMainMenuItemKey() + "] after update procedure.");
	    }
	}
	return updatedMainMenuItems;
    }

    /**
     * Purges (deletes from cloud) all menu items in the hierarchy of passed menu item including itself.
     *
     * @param mmi
     */
    private void purgeAll(final MainMenuItem mmi) {
	for (final MainMenuItem child : mmi.getChildren()) {
	    if (child.isPersisted()) {
		purgeAll(child);
	    }
	}
	mmiController.delete(mmi);
    }

    /**
     * Saves hierarchically menu item and its children.
     *
     * @param developmentMainMenuItem
     * @return
     */
    private MainMenuItem saveMenuItem(final MainMenuItem developmentMainMenuItem) {
	// System.err.println("Added menu item [" + developmentMainMenuItem + "].");
	final MainMenuItem itemToSave = factory.newByKey(MainMenuItem.class, developmentMainMenuItem.getKey());
	itemToSave.setDesc(developmentMainMenuItem.getDesc());
	itemToSave.setOrder(developmentMainMenuItem.getOrder());
	itemToSave.setTitle(developmentMainMenuItem.getTitle());
	itemToSave.setParent(developmentMainMenuItem.getParent() == null ? null : mmiController.findByKey(developmentMainMenuItem.getParent().getKey())); // should be updated instance!
	final MainMenuItem savedMainMenuItem = mmiController.save(itemToSave);

	// iterate through children hierarchy
	for (final MainMenuItem child : developmentMainMenuItem.getChildren()) {
	    savedMainMenuItem.addChild(saveMenuItem(child));
	}
	return savedMainMenuItem;
    }

    public User getUser() {
	return user;
    }

    public void setUser(final User user) {
	this.user = user;
    }
}
