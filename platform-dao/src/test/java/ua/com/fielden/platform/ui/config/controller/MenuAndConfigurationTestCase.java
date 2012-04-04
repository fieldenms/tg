package ua.com.fielden.platform.ui.config.controller;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuItemMixin;

/**
 * A test case for main application menu composition, persistence and management.
 *
 * @author TG Team
 *
 */
public class MenuAndConfigurationTestCase extends DbDrivenTestCase {
    private final IUserDao userDao = injector.getInstance(IUserDao.class);
    private final IEntityCentreConfigController eccController = injector.getInstance(IEntityCentreConfigController.class);
    private final IMainMenuItemController mmiController = injector.getInstance(IMainMenuItemController.class);

    private MainMenuItemMixin mixin = new MainMenuItemMixin(mmiController, eccController);

    @Test
    public void test_finding_for_principal_menu_items_in_case_of_base_user() {
	final User baseUser = userDao.findById(0L, new fetchAll<User>(User.class));
	mixin.setUser(baseUser);
	final List<MainMenuItem> menu = mixin.findPrincipalMenuItems();
	assertEquals("Incorrect number of principal items", 7, menu.size());
	for (final MainMenuItem item : menu) {
	    assertTrue("Incorrect principality", item.isPrincipal());
	    if (item.getId() == 5) {
		assertFalse("Incorrect visbility of item " + item.getId(), item.isVisible());
	    } else {
		assertTrue("Incorrect visbility", item.isVisible());
	    }

	}
    }

    @Test
    public void test_finding_for_principal_menu_items_in_case_of_descendant_user() {
	final User descendantUser = userDao.findById(1L, new fetchAll<User>(User.class));
	mixin.setUser(descendantUser);
	final List<MainMenuItem> menu = mixin.findPrincipalMenuItems();
	assertEquals("Incorrect number of principal items", 6, menu.size());
	for (final MainMenuItem item : menu) {
	    assertTrue("Incorrect principality", item.isPrincipal());
	    if (item.getId() == 5) {
		assertFalse("Incorrect visbility", item.isVisible());
	    } else {
		assertTrue("Incorrect visbility", item.isVisible());
	    }

	}
    }

    @Test
    public void test_finding_for_saveas_menu_items_in_case_of_base_user() {
	final User baseUser = userDao.findById(0L, new fetchAll<User>(User.class));
	mixin.setUser(baseUser);
	final List<MainMenuItem> menu = mixin.findSaveAsMenuItems();
	assertEquals("Incorrect number of 'save as' items", 1, menu.size());
	assertTrue("Should be visible because hierarchical visibility is not used here.", menu.get(0).isVisible());
    }

    @Test
    public void test_finding_for_saveas_menu_items_in_case_of_descendant_user() {
	final User descendantUser = userDao.findById(1L, new fetchAll<User>(User.class));
	mixin.setUser(descendantUser);
	final List<MainMenuItem> menu = mixin.findSaveAsMenuItems();
	assertEquals("Incorrect number of 'save as' items", 1, menu.size());
	assertTrue("Should be visible because hierarchical visibility is not used here.", menu.get(0).isVisible());
    }

    @Test
    public void test_constcution_of_menu_hierarchy_skeleton_for_base_user() {
	final User baseUser = userDao.findById(0L, new fetchAll<User>(User.class));
	mixin.setUser(baseUser);
	final List<MainMenuItem> menu = mixin.loadMenuSkeletonStructure();

	// assertions for root level items
	assertEquals("Incorrect number of root items", 2, menu.size());
	assertEquals("Incorrect first root item", Long.valueOf(0L), menu.get(0).getId());
	assertTrue("Incorrect visibility", menu.get(0).isVisible());
	assertTrue("Incorrect principality", menu.get(0).isPrincipal());

	assertEquals("Incorrect second root item", Long.valueOf(5L), menu.get(1).getId());
	assertFalse("Incorrect visibility", menu.get(1).isVisible());
	assertTrue("Incorrect principality", menu.get(1).isPrincipal());


	// assertions for second level items
	final List<MainMenuItem> secondLevelRoot1 = menu.get(0).getChildren();
	assertEquals("Incorrect number of items", 2, secondLevelRoot1.size());

	assertEquals("Incorrect item", Long.valueOf(1L), secondLevelRoot1.get(0).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1.get(0).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1.get(0).isPrincipal());

	assertEquals("Incorrect item", Long.valueOf(3L), secondLevelRoot1.get(1).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1.get(1).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1.get(1).isPrincipal());


	final List<MainMenuItem> secondLevelRoot2 = menu.get(1).getChildren();
	assertEquals("Incorrect number of items", 1, secondLevelRoot2.size());
	assertEquals("Incorrect item", Long.valueOf(6L), secondLevelRoot2.get(0).getId());
	assertFalse("Incorrect visibility", secondLevelRoot2.get(0).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1.get(0).isPrincipal());

	// assertions for third level, which includes 'save as items'
	final List<MainMenuItem> secondLevelRoot1_1 = secondLevelRoot1.get(0).getChildren();
	assertEquals("Incorrect number of items", 1, secondLevelRoot1_1.size());

	assertEquals("Incorrect item", Long.valueOf(2L), secondLevelRoot1_1.get(0).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1_1.get(0).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1_1.get(0).isPrincipal());
	assertTrue("Should not have sub items", secondLevelRoot1_1.get(0).getChildren().isEmpty());

	final List<MainMenuItem> secondLevelRoot1_2 = secondLevelRoot1.get(1).getChildren();
	assertEquals("Incorrect number of items", 1, secondLevelRoot1_2.size());

	assertEquals("Incorrect item", Long.valueOf(4L), secondLevelRoot1_2.get(0).getId());
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
	final User descendantUser = userDao.findById(1L, new fetchAll<User>(User.class));
	mixin.setUser(descendantUser);
	final List<MainMenuItem> menu = mixin.loadMenuSkeletonStructure();

	// assertions for root level items
	assertEquals("Incorrect number of root items", 1, menu.size());
	assertEquals("Incorrect first root item", Long.valueOf(0L), menu.get(0).getId());
	assertTrue("Incorrect visibility", menu.get(0).isVisible());
	assertTrue("Incorrect principality", menu.get(0).isPrincipal());

	// assertions for second level items
	final List<MainMenuItem> secondLevelRoot1 = menu.get(0).getChildren();
	assertEquals("Incorrect number of items", 2, secondLevelRoot1.size());

	assertEquals("Incorrect item", Long.valueOf(1L), secondLevelRoot1.get(0).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1.get(0).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1.get(0).isPrincipal());

	assertEquals("Incorrect item", Long.valueOf(3L), secondLevelRoot1.get(1).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1.get(1).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1.get(1).isPrincipal());

	// assertions for third level
	final List<MainMenuItem> secondLevelRoot1_1 = secondLevelRoot1.get(0).getChildren();
	assertEquals("Incorrect number of items", 1, secondLevelRoot1_1.size());

	assertEquals("Incorrect item", Long.valueOf(2L), secondLevelRoot1_1.get(0).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1_1.get(0).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1_1.get(0).isPrincipal());
	assertTrue("Should not have sub items", secondLevelRoot1_1.get(0).getChildren().isEmpty());

	final List<MainMenuItem> secondLevelRoot1_2 = secondLevelRoot1.get(1).getChildren();
	assertEquals("Incorrect number of items", 1, secondLevelRoot1_2.size());

	assertEquals("Incorrect item", Long.valueOf(4L), secondLevelRoot1_2.get(0).getId());
	assertTrue("Incorrect visibility", secondLevelRoot1_2.get(0).isVisible());
	assertTrue("Incorrect principality", secondLevelRoot1_2.get(0).isPrincipal());
	assertTrue("Should not have sub items", secondLevelRoot1_2.get(0).getChildren().isEmpty());
    }

    @Test
    public void test_parenthood_of_saveas_items() {
	final User user = userDao.findById(0L, new fetchAll<User>(User.class));
	mixin.setUser(user);
	final List<MainMenuItem> menu = mixin.loadMenuSkeletonStructure();

	final MainMenuItem parent2_1 = menu.get(1).getChildren().get(0); // item 2-1
	final List<MainMenuItem> chld = parent2_1.getChildren();

	for (final MainMenuItem ch : chld) {
	    assertTrue("Incorrect parent", parent2_1 == ch.getParent());
	}

    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/menu-and-configuration-test-case.flat.xml" };
    }

}
