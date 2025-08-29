package ua.com.fielden.platform.menu;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.CollectionUtil.*;

/**
 * A test case for menu invisibility logic.
 * 
 * @author TG Team
 *
 */
public class MenuProducerTest extends AbstractDaoTestCase {

    private final IMenuRetriever menuRetriever = new IMenuRetriever() {

        @Override
        public Menu getMenuEntity(final DeviceProfile deviceProfile) {
            final List<ModuleMenuItem> moduleMenu1 = listOf(
                    new ModuleMenuItem().setKey("module1item1"),
                    new ModuleMenuItem().setKey("module1item2"),
                    new ModuleMenuItem().setKey("module1item3"),
                    new ModuleMenuItem().setKey("module1item4"),
                    new ModuleMenuItem().setKey("module1item5"));
            final ModuleMenu module1 = new ModuleMenu().setKey("module1").setMenu(moduleMenu1);

            final List<ModuleMenuItem> module2group1 = listOf(
                    new ModuleMenuItem().setKey("module2group1item1"),
                    new ModuleMenuItem().setKey("module2group1item2"));
            final List<ModuleMenuItem> module2group2 = listOf(
                    new ModuleMenuItem().setKey("module2group2item1"),
                    new ModuleMenuItem().setKey("module2group2item2"),
                    new ModuleMenuItem().setKey("module2group2item3"));
            final List<ModuleMenuItem> moduleMenu2 = listOf(
                    new ModuleMenuItem().setKey("module2group1").setMenu(module2group1),
                    new ModuleMenuItem().setKey("module2item1"),
                    new ModuleMenuItem().setKey("module2item2"),
                    new ModuleMenuItem().setKey("module2group2").setMenu(module2group2));
            final ModuleMenu module2 = new ModuleMenu().setKey("module2").setMenu(moduleMenu2);

            return new Menu().setMenu(listOf(module1, module2));
        }
    };

    @Test
    public void menu_restored_with_base_user_has_all_menu_items_with_appropriate_visible_property() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final WebMenuItemInvisibilityCo mii = co(WebMenuItemInvisibility.class);

        up.setUser(co(User.class).findByKey("BUSER_1"));
        save(new_(MenuSaveAction.class)
                .setInvisibleMenuItems(setOf(
                        "module1/module1item3",
                        "module2/module2group1/module2group1item1",
                        "module2/module2group1/module2group1item2",
                        "module2/module2item2",
                        "module2/module2group2/module2group2item2",
                        "module2/module2group2/module2group2item3")));

        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up, getInstance(ICompanionObjectFinder.class), getInstance(EntityFactory.class), getInstance(IApplicationSettings.class), "", "183");
        final CentreContext context = new CentreContext();
        context.setChosenProperty("desktop");
        menuProducer.setContext(context);
        final Menu menu = menuProducer.newEntity();

        assertEquals("The menu has incorrect number of modules", 2, menu.getMenu().size());

        final List<ModuleMenu> modules = menu.getMenu();
        for (int moduleIndex = 0; moduleIndex < 2; moduleIndex++) {
            assertEquals("The module key is incorrect", "module" + (moduleIndex + 1), modules.get(moduleIndex).getKey());
        }

        assertEquals("The menu of the module1 has incorrect size", 5, modules.get(0).getMenu().size());
        assertEquals("The menu key in module1 is incorrect", "module1item1", modules.get(0).getMenu().get(0).getKey());
        assertTrue("The menu item1 in module1 should be visible", modules.get(0).getMenu().get(0).isVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item2", modules.get(0).getMenu().get(1).getKey());
        assertTrue("The menu item2 in module1 should be visible", modules.get(0).getMenu().get(1).isVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item3", modules.get(0).getMenu().get(2).getKey());
        assertFalse("The menu item3 in module1 should be invisible", modules.get(0).getMenu().get(2).isVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item4", modules.get(0).getMenu().get(3).getKey());
        assertTrue("The menu item4 in module1 should be visible", modules.get(0).getMenu().get(0).isVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item5", modules.get(0).getMenu().get(4).getKey());
        assertTrue("The menu item5 in module1 should be visible", modules.get(0).getMenu().get(0).isVisible());

        assertEquals("The menu of the module2 has incorrect size", 4, modules.get(1).getMenu().size());
        assertEquals("The menu key in module2 is incorrect", "module2group1", modules.get(1).getMenu().get(0).getKey());
        assertFalse("The menu group1 in module2 should be invisible", modules.get(1).getMenu().get(0).isVisible());
        assertEquals("The menu group1 in module2 has incorrect number of menu items", 2, modules.get(1).getMenu().get(0).getMenu().size());
        final List<ModuleMenuItem> group1 = modules.get(1).getMenu().get(0).getMenu();
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item1", group1.get(0).getKey());
        assertFalse("The menu group1item1 in module2 should be invisible", group1.get(0).isVisible());
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item2", group1.get(1).getKey());
        assertFalse("The menu group1item2 in module2 should be invisible", group1.get(1).isVisible());

        assertEquals("The menu key in module2 is incorrect", "module2item1", modules.get(1).getMenu().get(1).getKey());
        assertTrue("The menu item1 in module2 should be visible", modules.get(1).getMenu().get(1).isVisible());
        assertEquals("The menu key in module2 is incorrect", "module2item2", modules.get(1).getMenu().get(2).getKey());
        assertFalse("The menu item2 in module2 should be invisible", modules.get(1).getMenu().get(2).isVisible());

        assertEquals("The menu key in module2 is incorrect", "module2group2", modules.get(1).getMenu().get(3).getKey());
        assertEquals("The menu group2 in module2 has incorrect number of menu items", 3, modules.get(1).getMenu().get(3).getMenu().size());
        assertTrue("The menu group1 in module2 should be visible", modules.get(1).getMenu().get(3).isVisible());
        final List<ModuleMenuItem> group2 = modules.get(1).getMenu().get(3).getMenu();
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item1", group2.get(0).getKey());
        assertTrue("The menu group2item1 in module2 should be visible", group2.get(0).isVisible());
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item2", group2.get(1).getKey());
        assertFalse("The menu group2item2 in module2 should be invisible", group2.get(1).isVisible());
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item3", group2.get(2).getKey());
        assertFalse("The menu group2item3 in module2 should be invisible", group2.get(2).isVisible());
    }

    @Test
    public void menu_restored_with_based_on_user_has_no_invisible_menu_items() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final WebMenuItemInvisibilityCo mii = co(WebMenuItemInvisibility.class);

        up.setUser(co(User.class).findByKey("BUSER_1"));
        save(new_(MenuSaveAction.class)
                .setInvisibleMenuItems(setOf(
                        "module1/module1item3",
                        "module2/module2group1/module2group1item1",
                        "module2/module2group1/module2group1item2",
                        "module2/module2item2",
                        "module2/module2group2/module2group2item2",
                        "module2/module2group2/module2group2item3")));

        checkMenuVisibilityForUser("USER_1", up, mii);
        checkMenuVisibilityForUser("USER_2", up, mii);
    }

    private void checkMenuVisibilityForUser(final String user, final IUserProvider up, final WebMenuItemInvisibilityCo mii) {
        up.setUser(co(User.class).findByKey(user));
        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up, getInstance(ICompanionObjectFinder.class), getInstance(EntityFactory.class), getInstance(IApplicationSettings.class), "", "183");
        final CentreContext context = new CentreContext();
        context.setChosenProperty("desktop");
        menuProducer.setContext(context);
        final Menu menu = menuProducer.newEntity();

        assertEquals("The menu has incorrect number of modules", 2, menu.getMenu().size());

        final List<ModuleMenu> modules = menu.getMenu();
        for (int moduleIndex = 0; moduleIndex < 2; moduleIndex++) {
            assertEquals("The module key is incorrect", "module" + (moduleIndex + 1), modules.get(moduleIndex).getKey());
        }

        assertEquals("The menu of the module1 has incorrect size", 4, modules.get(0).getMenu().size());
        assertEquals("The menu key in module1 is incorrect", "module1item1", modules.get(0).getMenu().get(0).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item2", modules.get(0).getMenu().get(1).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item4", modules.get(0).getMenu().get(2).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item5", modules.get(0).getMenu().get(3).getKey());

        assertEquals("The menu of the module2 has incorrect size", 2, modules.get(1).getMenu().size());
        assertEquals("The menu key in module2 is incorrect", "module2item1", modules.get(1).getMenu().get(0).getKey());
        assertEquals("The menu key in module2 is incorrect", "module2group2", modules.get(1).getMenu().get(1).getKey());
        assertEquals("The menu group2 in module2 has incorrect number of menu items", 1, modules.get(1).getMenu().get(1).getMenu().size());
        final List<ModuleMenuItem> group = modules.get(1).getMenu().get(1).getMenu();
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item1", group.get(0).getKey());
    }

    @Test
    public void menu_configured_for_one_based_on_user_has_no_effect_on_other_based_on_users() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final WebMenuItemInvisibilityCo mii = co(WebMenuItemInvisibility.class);

        up.setUser(co(User.class).findByKey("BUSER_1"));
        makeInvisibleMenuItemsForUser(setOf(
                "module1/module1item3",
                "module2/module2group1/module2group1item1",
                "module2/module2group1/module2group1item2",
                "module2/module2item2",
                "module2/module2group2/module2group2item2",
                "module2/module2group2/module2group2item3"), "USER_1");

        checkMenuVisibilityForUser("USER_1", up, mii);

        up.setUser(co(User.class).findByKey("USER_2"));

        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up, getInstance(ICompanionObjectFinder.class), getInstance(EntityFactory.class), getInstance(IApplicationSettings.class), "", "183");
        final CentreContext context = new CentreContext();
        context.setChosenProperty("desktop");
        menuProducer.setContext(context);
        final Menu menu = menuProducer.newEntity();

        assertEquals("The menu has incorrect number of modules", 2, menu.getMenu().size());

        final List<ModuleMenu> modules = menu.getMenu();
        for (int moduleIndex = 0; moduleIndex < 2; moduleIndex++) {
            assertEquals("The module key is incorrect", "module" + (moduleIndex + 1), modules.get(moduleIndex).getKey());
        }
        //Module 1 Check
        assertEquals("The menu of the module1 has incorrect size", 5, modules.get(0).getMenu().size());
        assertEquals("The menu key in module1 is incorrect", "module1item1", modules.get(0).getMenu().get(0).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item2", modules.get(0).getMenu().get(1).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item3", modules.get(0).getMenu().get(2).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item4", modules.get(0).getMenu().get(3).getKey());
        assertEquals("The menu key in module1 is incorrect", "module1item5", modules.get(0).getMenu().get(4).getKey());
        //Module 2 check
        assertEquals("The menu of the module2 has incorrect size", 4, modules.get(1).getMenu().size());
        //Group 1 of module 2 check
        assertEquals("The menu key in module2 is incorrect", "module2group1", modules.get(1).getMenu().get(0).getKey());
        assertEquals("The menu group1 in module2 has incorrect number of menu items", 2, modules.get(1).getMenu().get(0).getMenu().size());
        final List<ModuleMenuItem> group1 = modules.get(1).getMenu().get(0).getMenu();
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item1", group1.get(0).getKey());
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item2", group1.get(1).getKey());
        //Menu items of module 2 check
        assertEquals("The menu key in module2 is incorrect", "module2item1", modules.get(1).getMenu().get(1).getKey());
        assertEquals("The menu key in module2 is incorrect", "module2item2", modules.get(1).getMenu().get(2).getKey());
        //Group 2 of module 2 check
        assertEquals("The menu key in module2 is incorrect", "module2group2", modules.get(1).getMenu().get(3).getKey());
        assertEquals("The menu group2 in module2 has incorrect number of menu items", 3, modules.get(1).getMenu().get(3).getMenu().size());
        final List<ModuleMenuItem> group2 = modules.get(1).getMenu().get(3).getMenu();
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item1", group2.get(0).getKey());
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item2", group2.get(1).getKey());
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item3", group2.get(2).getKey());
    }

    private void makeInvisibleMenuItemsForUser(final Set<String> menuItems, final String userName) {
        final UserMenuVisibilityAssociatorCo umiaCo = co(UserMenuVisibilityAssociator.class);
        final UserMenuVisibilityAssociatorProducer umvProducer = new UserMenuVisibilityAssociatorProducer(getInstance(EntityFactory.class), getInstance(IUserProvider.class), getInstance(ICompanionObjectFinder.class));
        final User userToExclude = co(User.class).findByKey(userName);

        menuItems.forEach(menuItem -> {
            final CentreContext context = new CentreContext();
            context.setSelectedEntities(listOf(new WebMenuItemInvisibility().setMenuItemUri(menuItem)));
            umvProducer.setContext(context);
            final UserMenuVisibilityAssociator newEntity = umvProducer.newEntity();
            newEntity.setRemovedIds(linkedSetOf(userToExclude.getId()));
            umiaCo.save(newEntity);
        });
    }

    @Test
    public void rebased_active_based_on_users_do_not_see_invisible_menu_items() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final WebMenuItemInvisibilityCo mii = co(WebMenuItemInvisibility.class);
        final IUser coUser = co(User.class);

        final User baseUser = coUser.findByKey("BUSER_2");
        up.setUser(baseUser);
        save(new_(MenuSaveAction.class).setInvisibleMenuItems(setOf("module2/module2group1/module2group1item1")));

        up.setUser(coUser.findByKey("BUSER_1"));
        makeInvisibleMenuItemsForUser(setOf("module2/module2group1/module2group1item2"), "USER_1");

        up.setUser(coUser.findByKey(UNIT_TEST_USER));
        final IUser co$User = co$(User.class);
        save(co$User.findByKeyAndFetch(coUser.getFetchProvider().fetchModel(),"USER_1").setBasedOnUser(baseUser));
        save(new_(User.class, "USER_6").setBase(false).setActive(true).setEmail("user6@mail").setBasedOnUser(baseUser));
        save(co$User.findByKeyAndFetch(coUser.getFetchProvider().fetchModel(),"USER_5").setActive(true));

        assertMenuInvisibilityForUser("USER_1", up, mii);
        assertMenuInvisibilityForUser("USER_6", up, mii);
        assertMenuInvisibilityForUser("USER_5", up, mii);
    }

    private void assertMenuInvisibilityForUser(final String userName, final IUserProvider up, final WebMenuItemInvisibilityCo mii) {
        up.setUser(co(User.class).findByKey("USER_1"));
        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up, getInstance(ICompanionObjectFinder.class), getInstance(EntityFactory.class), getInstance(IApplicationSettings.class), "", "183");
        final CentreContext context = new CentreContext();
        context.setChosenProperty("desktop");
        menuProducer.setContext(context);
        final Menu menu = menuProducer.newEntity();

        final ModuleMenuItem module2group1 = menu.getMenu().get(1).getMenu().get(0);

        assertEquals("The menu group1 in module2 has incorrect number of menu items", 1, module2group1.getMenu().size());
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item2", module2group1.getMenu().get(0).getKey());
    }

    @Test
    public void rebased_active_based_on_user_can_see_menu_item_that_is_semi_visible() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final WebMenuItemInvisibilityCo mii = co(WebMenuItemInvisibility.class);
        final IUser coUser = co(User.class);

        final User baseUser = coUser.findByKey("BUSER_2");
        up.setUser(baseUser);
        makeInvisibleMenuItemsForUser(setOf("module2/module2group1/module2group1item1"), "USER_3");

        up.setUser(coUser.findByKey(UNIT_TEST_USER));
        final IUser co$User = co$(User.class);
        save(co$User.findByKeyAndFetch(coUser.getFetchProvider().fetchModel(),"USER_1").setBasedOnUser(baseUser));
        save(new_(User.class, "USER_6").setBase(false).setActive(true).setEmail("user6@mail").setBasedOnUser(baseUser));
        save(co$User.findByKeyAndFetch(coUser.getFetchProvider().fetchModel(),"USER_5").setActive(true));

        assertMenuVisibilityForUser("USER_1", up, mii);
        assertMenuVisibilityForUser("USER_6", up, mii);
        assertMenuVisibilityForUser("USER_5", up, mii);
    }

    private void assertMenuVisibilityForUser(final String userName, final IUserProvider up, final WebMenuItemInvisibilityCo mii) {
        up.setUser(co(User.class).findByKey(userName));
        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up, getInstance(ICompanionObjectFinder.class), getInstance(EntityFactory.class), getInstance(IApplicationSettings.class), "", "183");
        final CentreContext context = new CentreContext();
        context.setChosenProperty("desktop");
        menuProducer.setContext(context);
        final Menu menu = menuProducer.newEntity();

        final ModuleMenuItem module2group1 = menu.getMenu().get(1).getMenu().get(0);

        assertEquals("The menu group1 in module2 has incorrect number of menu items", 2, module2group1.getMenu().size());
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item1", module2group1.getMenu().get(0).getKey());
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item2", module2group1.getMenu().get(1).getKey());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        //Creating base and based on users
        final User user1 = save(new_(User.class, "BUSER_1").setBase(true).setActive(true).setEmail("buser1@mail"));
        save(new_(User.class, "USER_1").setBase(false).setActive(true).setEmail("user1@mail").setBasedOnUser(user1));
        save(new_(User.class, "USER_2").setBase(false).setActive(true).setEmail("user2@mail").setBasedOnUser(user1));
        final User user2 = save(new_(User.class, "BUSER_2").setBase(true).setActive(true).setEmail("buser2@mail"));
        save(new_(User.class, "USER_3").setBase(false).setActive(true).setEmail("user3@mail").setBasedOnUser(user2));
        save(new_(User.class, "USER_4").setBase(false).setActive(true).setEmail("user4@mail").setBasedOnUser(user2));
        save(new_(User.class, "USER_5").setBase(false).setActive(false).setEmail("user5@mail").setBasedOnUser(user2));
    }

}