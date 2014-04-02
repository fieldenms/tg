package ua.com.fielden.platform.ui.config.controller.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenu;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;

import com.google.inject.Inject;

/**
 * A mixin to avoid code duplication for providing RAO and DAO implementations of {@link IMainMenuItemController}.
 * 
 * @author TG Team
 * 
 */
public final class MainMenuItemMixin {
    private final IMainMenu mmController;
    private final IMainMenuItemController mmiController;
    private final IEntityCentreConfigController eccController;
    private final IEntityCentreAnalysisConfig ecacController;
    private final IMainMenuItemInvisibilityController mmiiController;
    private final EntityFactory factory;
    private User user; // should be set upon user successful login.
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    public MainMenuItemMixin(final IMainMenu mmController, final IMainMenuItemController mmiController, final IEntityCentreConfigController eccController, final IEntityCentreAnalysisConfig ecacController, final IMainMenuItemInvisibilityController mmiiController, final EntityFactory factory) {
        this.mmController = mmController;
        this.mmiController = mmiController;
        this.eccController = eccController;
        this.ecacController = ecacController;
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
        Period pd = null;
        DateTime st = null;

        st = new DateTime();
        final List<MainMenuItem> allItems = findPrincipalMenuItems();
        pd = new Period(st, new DateTime());
        logger.info("Find principal menu items...done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");

        st = new DateTime();
        allItems.addAll(findSaveAsMenuItems());
        pd = new Period(st, new DateTime());
        logger.info("Find saveAs menu items...done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");

        st = new DateTime();
        // once there is a linear list of menu items need to build a hierarchical structure
        final List<MainMenuItem> result = buildMenuHierarchy(allItems);
        pd = new Period(st, new DateTime());
        logger.info("Build menu hierarchy...done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
        return result;
    }

    /**
     * Obtains a linear list of all menu items stored in the cloud. Obtains the list of invisible for the current user items. Sets visibility of the items in the linear list based
     * on this result, which depends on whether user is a base user or otherwise. In case of a non-base user, invisible menu items are removed from the resultant list returned by
     * this method.
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

    /**
     * Updates existing persisted menu items with new "development" ones. This method is only applicable for base user, which is capable for menu items modification.
     * <p>
     * It covers correct handling of menu items tree structure including: <br>
     * 1. adding new items; <br>
     * 2. removing old items (<b>IMPORTANT:</b> all tree menu structure will be purged including entity-centres with "saveAsses" and all item invisibility information); <br>
     * 3. modification of old items aka "item type = key" modification (<b>IMPORTANT:</b> in case of <b>PRINCIPLE</b> item modification -- all tree menu structure will be purged
     * including entity-centres with "saveAsses" and all item invisibility information); <br>
     * 4. items order modification. <br>
     * 
     * @param developmentMainMenuStructureBuilder
     * @param username
     * @return
     */
    public final void updateMenuItemsWithDevelopmentOnes(final IMainMenuStructureBuilder developmentMainMenuStructureBuilder) {
        if (!user.isBase()) {
            throw new IllegalArgumentException("Updating of menu items is not permitted for non-base user. Current user [" + user + "] is not base user.");
        }

        final MainMenu mainMenu = factory.newByKey(MainMenu.class, "IRRELEVANT");
        mainMenu.setMenuItems(MainMenuStructureFactory.toStrings(developmentMainMenuStructureBuilder.build()));
        mmController.save(mainMenu);
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }
}
