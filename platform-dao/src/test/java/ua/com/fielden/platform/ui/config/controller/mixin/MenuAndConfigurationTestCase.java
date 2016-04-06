package ua.com.fielden.platform.ui.config.controller.mixin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;

/**
 * A test case for main application menu composition, persistence and management.
 * 
 * @author TG Team
 * 
 */
public class MenuAndConfigurationTestCase extends AbstractDomainDrivenTestCase {
    private final IUser userDao = getInstance(IUser.class);
    private final MainMenuItemMixin mixin = new MainMenuItemMixin(getInstance(IMainMenu.class), getInstance(IMainMenuItemController.class), getInstance(IEntityCentreConfigController.class), getInstance(IEntityCentreAnalysisConfig.class), getInstance(IMainMenuItemInvisibilityController.class), getInstance(EntityFactory.class));

    private User getBaseUser() {
        return userDao.findByKeyAndFetch(fetchAll(User.class), "BUSER");
    }

    private User getDescendantUser() {
        return userDao.findByKeyAndFetch(fetchAll(User.class), "DUSER");
    }

    @Test
    public void test_finding_for_principal_menu_items_in_case_of_base_user() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        final List<MainMenuItem> menu = mixin.findPrincipalMenuItems();
        assertEquals("Incorrect number of principal items", 7, menu.size());
        for (final MainMenuItem item : menu) {
            assertTrue("Incorrect principality", item.isPrincipal());
            if ("type6".equals(item.getKey())) {
                assertFalse("Incorrect visbility of item " + item.getKey(), item.isVisible());
            } else {
                assertTrue("Incorrect visbility", item.isVisible());
            }

        }
    }

    @Test
    public void test_finding_for_principal_menu_items_in_case_of_descendant_user() {
        final User descendantUser = getDescendantUser();
        mixin.setUser(descendantUser);
        final List<MainMenuItem> menu = mixin.findPrincipalMenuItems();
        assertEquals("Incorrect number of principal items", 6, menu.size());
        for (final MainMenuItem item : menu) {
            assertTrue("Incorrect principality", item.isPrincipal());
            if ("type6".equals(item.getKey())) {
                assertFalse("Incorrect visbility", item.isVisible());
            } else {
                assertTrue("Incorrect visbility", item.isVisible());
            }

        }
    }

    @Test
    public void test_finding_for_saveas_menu_items_in_case_of_base_user() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        final List<MainMenuItem> menu = mixin.findSaveAsMenuItems();
        assertEquals("Incorrect number of 'save as' items", 1, menu.size());
        assertTrue("Should be visible because hierarchical visibility is not used here.", menu.get(0).isVisible());
    }

    @Test
    public void test_finding_for_saveas_menu_items_in_case_of_descendant_user() {
        final User descendantUser = getDescendantUser();
        mixin.setUser(descendantUser);
        final List<MainMenuItem> menu = mixin.findSaveAsMenuItems();
        assertEquals("Incorrect number of 'save as' items", 1, menu.size());
        assertTrue("Should be visible because hierarchical visibility is not used here.", menu.get(0).isVisible());
    }

    @Test
    public void test_constcution_of_menu_hierarchy_skeleton_for_base_user() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        final List<MainMenuItem> menu = mixin.loadMenuSkeletonStructure();

        // assertions for root level items
        assertEquals("Incorrect number of root items", 2, menu.size());
        assertEquals("Incorrect first root item", "type1", menu.get(0).getKey());
        assertTrue("Incorrect visibility", menu.get(0).isVisible());
        assertTrue("Incorrect principality", menu.get(0).isPrincipal());

        assertEquals("Incorrect second root item", "type6", menu.get(1).getKey());
        assertFalse("Incorrect visibility", menu.get(1).isVisible());
        assertTrue("Incorrect principality", menu.get(1).isPrincipal());

        // assertions for second level items
        final List<MainMenuItem> secondLevelRoot1 = menu.get(0).getChildren();
        assertEquals("Incorrect number of items", 2, secondLevelRoot1.size());

        assertEquals("Incorrect item", "type2", secondLevelRoot1.get(0).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1.get(0).isPrincipal());

        assertEquals("Incorrect item", "type4", secondLevelRoot1.get(1).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1.get(1).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1.get(1).isPrincipal());

        final List<MainMenuItem> secondLevelRoot2 = menu.get(1).getChildren();
        assertEquals("Incorrect number of items", 1, secondLevelRoot2.size());
        assertEquals("Incorrect item", "type7", secondLevelRoot2.get(0).getKey());
        assertFalse("Incorrect visibility", secondLevelRoot2.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1.get(0).isPrincipal());

        // assertions for third level, which includes 'save as items'
        final List<MainMenuItem> secondLevelRoot1_1 = secondLevelRoot1.get(0).getChildren();
        assertEquals("Incorrect number of items", 1, secondLevelRoot1_1.size());

        assertEquals("Incorrect item", "type3", secondLevelRoot1_1.get(0).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1_1.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1_1.get(0).isPrincipal());
        assertTrue("Should not have sub items", secondLevelRoot1_1.get(0).getChildren().isEmpty());

        final List<MainMenuItem> secondLevelRoot1_2 = secondLevelRoot1.get(1).getChildren();
        assertEquals("Incorrect number of items", 1, secondLevelRoot1_2.size());

        assertEquals("Incorrect item", "type5", secondLevelRoot1_2.get(0).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1_2.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1_2.get(0).isPrincipal());
        assertTrue("Should not have sub items", secondLevelRoot1_2.get(0).getChildren().isEmpty());

        final List<MainMenuItem> secondLevelRoot2_1 = secondLevelRoot2.get(0).getChildren();
        assertEquals("Incorrect number of items", 1, secondLevelRoot2_1.size());

        assertNull("Incorrect item", secondLevelRoot2_1.get(0).getId());
        assertFalse("Incorrect visibility", secondLevelRoot2_1.get(0).isVisible());
        assertFalse("Incorrect principality", secondLevelRoot2_1.get(0).isPrincipal());
        assertTrue("Should not have sub items", secondLevelRoot2_1.get(0).getChildren().isEmpty());
        assertNotNull("Should have an accosiated configuration", secondLevelRoot2_1.get(0).getConfig());
        assertEquals("Incorrect key value for a 'save as' menu item", secondLevelRoot2.get(0).getKey(), secondLevelRoot2_1.get(0).getKey());
    }

    @Test
    public void test_constcution_of_menu_hierarchy_skeleton_for_descendant_user() {
        final User descendantUser = getDescendantUser();
        mixin.setUser(descendantUser);
        final List<MainMenuItem> menu = mixin.loadMenuSkeletonStructure();

        // assertions for root level items
        assertEquals("Incorrect number of root items", 1, menu.size());
        assertEquals("Incorrect first root item", "type1", menu.get(0).getKey());
        assertTrue("Incorrect visibility", menu.get(0).isVisible());
        assertTrue("Incorrect principality", menu.get(0).isPrincipal());

        // assertions for second level items
        final List<MainMenuItem> secondLevelRoot1 = menu.get(0).getChildren();
        assertEquals("Incorrect number of items", 2, secondLevelRoot1.size());

        assertEquals("Incorrect item", "type2", secondLevelRoot1.get(0).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1.get(0).isPrincipal());

        assertEquals("Incorrect item", "type4", secondLevelRoot1.get(1).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1.get(1).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1.get(1).isPrincipal());

        // assertions for third level
        final List<MainMenuItem> secondLevelRoot1_1 = secondLevelRoot1.get(0).getChildren();
        assertEquals("Incorrect number of items", 1, secondLevelRoot1_1.size());

        assertEquals("Incorrect item", "type3", secondLevelRoot1_1.get(0).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1_1.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1_1.get(0).isPrincipal());
        assertTrue("Should not have sub items", secondLevelRoot1_1.get(0).getChildren().isEmpty());

        final List<MainMenuItem> secondLevelRoot1_2 = secondLevelRoot1.get(1).getChildren();
        assertEquals("Incorrect number of items", 1, secondLevelRoot1_2.size());

        assertEquals("Incorrect item", "type5", secondLevelRoot1_2.get(0).getKey());
        assertTrue("Incorrect visibility", secondLevelRoot1_2.get(0).isVisible());
        assertTrue("Incorrect principality", secondLevelRoot1_2.get(0).isPrincipal());
        assertTrue("Should not have sub items", secondLevelRoot1_2.get(0).getChildren().isEmpty());
    }

    @Test
    public void test_parenthood_of_saveas_items() {
        final User user = getBaseUser();
        mixin.setUser(user);
        final List<MainMenuItem> menu = mixin.loadMenuSkeletonStructure();

        final MainMenuItem parent2_1 = menu.get(1).getChildren().get(0); // item 2-1
        final List<MainMenuItem> chld = parent2_1.getChildren();

        for (final MainMenuItem ch : chld) {
            assertTrue("Incorrect parent", parent2_1 == ch.getParent());
        }
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformDomainTypes.types;
    }

    @Override
    protected void populateDomain() {
        final User baseUser = save(new_(User.class, "BUSER").setBase(true)); // base user
        save(new_(User.class, "DUSER").setBase(false).setBasedOnUser(baseUser)); // descendant user

        // populate main menu items
        final MainMenuItem root_1 = save(new_(MainMenuItem.class, "type1").setTitle("Root 1").setOrder(1));
        /**/final MainMenuItem item_1_1 = /**/save(new_(MainMenuItem.class, "type2").setParent(root_1).setTitle("Item 1-1").setOrder(1));
        /*    */save(new_(MainMenuItem.class, "type3").setParent(item_1_1).setTitle("Item 1-1-1").setOrder(1));
        /**/final MainMenuItem item_1_2 = /**/save(new_(MainMenuItem.class, "type4").setParent(root_1).setTitle("Item 1-2").setOrder(2));
        /*    */save(new_(MainMenuItem.class, "type5").setParent(item_1_2).setTitle("Item 1-2-1").setOrder(1));
        final MainMenuItem root_2 = save(new_(MainMenuItem.class, "type6").setTitle("Root 2").setOrder(2)); // should be recognized as invisible
        /**/final MainMenuItem item_2_1 = /**/save(new_(MainMenuItem.class, "type7").setParent(root_2).setTitle("Item 2-1").setOrder(1)); // should be recognized as invisible

        // populate invisibility
        save(new_composite(MainMenuItemInvisibility.class, baseUser, root_2)); // should make principal items 5, 6 and "save as" item 0 not visible

        // populate entity centres
        /**/save(new_composite(EntityCentreConfig.class, baseUser, "principal for item 2-1", item_2_1).setPrincipal(true));
        /*    */save(new_composite(EntityCentreConfig.class, baseUser, "save as for item 2-1", item_2_1).setPrincipal(false));
    }
}
