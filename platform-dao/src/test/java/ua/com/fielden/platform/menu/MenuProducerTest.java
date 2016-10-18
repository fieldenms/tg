package ua.com.fielden.platform.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class MenuProducerTest extends AbstractDaoTestCase {

    private final IMenuRetriever menuRetriever = new IMenuRetriever() {

        @Override
        public Menu getMenuEntity() {
            final List<ModuleMenuItem> moduleMenu1 = Arrays.asList(
                    new ModuleMenuItem().setKey("module1item1"),
                    new ModuleMenuItem().setKey("module1item2"),
                    new ModuleMenuItem().setKey("module1item3"),
                    new ModuleMenuItem().setKey("module1item4"),
                    new ModuleMenuItem().setKey("module1item5"));
            final Module module1 = new Module().setKey("module1").setMenu(moduleMenu1);

            final List<ModuleMenuItem> module2group1 = Arrays.asList(
                    new ModuleMenuItem().setKey("module2group1item1"),
                    new ModuleMenuItem().setKey("module2group1item2"));
            final List<ModuleMenuItem> module2group2 = Arrays.asList(
                    new ModuleMenuItem().setKey("module2group2item1"),
                    new ModuleMenuItem().setKey("module2group2item2"),
                    new ModuleMenuItem().setKey("module2group2item3"));
            final List<ModuleMenuItem> moduleMenu2 = Arrays.asList(
                    new ModuleMenuItem().setKey("module2group1").setMenu(module2group1),
                    new ModuleMenuItem().setKey("module2item1"),
                    new ModuleMenuItem().setKey("module2item2"),
                    new ModuleMenuItem().setKey("module2group2").setMenu(module2group2));
            final Module module2 = new Module().setKey("module2").setMenu(moduleMenu2);

            final List<Module> modules = Arrays.asList(module1, module2);
            final Menu menu = new Menu().setMenu(modules);
            return menu;
        }
    };

    @Test
    public void testMenuRestorationForBaseUser() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final IWebMenuItemInvisibility mii = co(WebMenuItemInvisibility.class);

        up.setUser(co(User.class).findByKey("USER_1"));
        save(new_(MenuSaveAction.class)
                .setInvisibleMenuItems(new HashSet<>(Arrays.asList(
                        "module1/module1item3",
                        "module2/module2group1",
                        "module2/module2group1/module2group1item1",
                        "module2/module2group1/module2group1item2",
                        "module2/module2item2",
                        "module2/module2group2/module2group2item2",
                        "module2/module2group2/module2group2item3"))));

        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up);
        final Menu menu = menuProducer.newEntity();

        assertEquals("The menu has incorrect number of modules", 2, menu.getMenu().size());

        final List<Module> modules = menu.getMenu();
        for (int moduleIndex = 0; moduleIndex < 2; moduleIndex++) {
            assertEquals("The module key is incorrect", "module" + (moduleIndex + 1), modules.get(moduleIndex).getKey());
        }

        assertEquals("The menu of the module1 has incorrect size", 5, modules.get(0).getMenu().size());
        assertEquals("The menu key in module1 is incorrect", "module1item1", modules.get(0).getMenu().get(0).getKey());
        assertTrue("The menu item1 in module1 should be visible", modules.get(0).getMenu().get(0).getIsVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item2", modules.get(0).getMenu().get(1).getKey());
        assertTrue("The menu item2 in module1 should be visible", modules.get(0).getMenu().get(1).getIsVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item3", modules.get(0).getMenu().get(2).getKey());
        assertFalse("The menu item3 in module1 should be invisible", modules.get(0).getMenu().get(2).getIsVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item4", modules.get(0).getMenu().get(3).getKey());
        assertTrue("The menu item4 in module1 should be visible", modules.get(0).getMenu().get(0).getIsVisible());
        assertEquals("The menu key in module1 is incorrect", "module1item5", modules.get(0).getMenu().get(4).getKey());
        assertTrue("The menu item5 in module1 should be visible", modules.get(0).getMenu().get(0).getIsVisible());

        assertEquals("The menu of the module2 has incorrect size", 4, modules.get(1).getMenu().size());
        assertEquals("The menu key in module2 is incorrect", "module2group1", modules.get(1).getMenu().get(0).getKey());
        assertFalse("The menu group1 in module2 should be invisible", modules.get(1).getMenu().get(0).getIsVisible());
        assertEquals("The menu group1 in module2 has incorrect number of menu items", 2, modules.get(1).getMenu().get(0).getMenu().size());
        final List<ModuleMenuItem> group1 = modules.get(1).getMenu().get(0).getMenu();
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item1", group1.get(0).getKey());
        assertFalse("The menu group1item1 in module2 should be invisible", group1.get(0).getIsVisible());
        assertEquals("The menu key in module2group1 is incorrect", "module2group1item2", group1.get(1).getKey());
        assertFalse("The menu group1item2 in module2 should be invisible", group1.get(1).getIsVisible());

        assertEquals("The menu key in module2 is incorrect", "module2item1", modules.get(1).getMenu().get(1).getKey());
        assertTrue("The menu item1 in module2 should be visible", modules.get(1).getMenu().get(1).getIsVisible());
        assertEquals("The menu key in module2 is incorrect", "module2item2", modules.get(1).getMenu().get(2).getKey());
        assertFalse("The menu item2 in module2 should be invisible", modules.get(1).getMenu().get(2).getIsVisible());

        assertEquals("The menu key in module2 is incorrect", "module2group2", modules.get(1).getMenu().get(3).getKey());
        assertEquals("The menu group2 in module2 has incorrect number of menu items", 3, modules.get(1).getMenu().get(3).getMenu().size());
        assertTrue("The menu group1 in module2 should be visible", modules.get(1).getMenu().get(3).getIsVisible());
        final List<ModuleMenuItem> group2 = modules.get(1).getMenu().get(3).getMenu();
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item1", group2.get(0).getKey());
        assertTrue("The menu group2item1 in module2 should be visible", group2.get(0).getIsVisible());
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item2", group2.get(1).getKey());
        assertFalse("The menu group2item2 in module2 should be invisible", group2.get(1).getIsVisible());
        assertEquals("The menu key in module2group2 is incorrect", "module2group2item3", group2.get(2).getKey());
        assertFalse("The menu group2item3 in module2 should be invisible", group2.get(2).getIsVisible());
    }

    @Test
    public void testMenuRestorationForNonBaseUser() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final IWebMenuItemInvisibility mii = co(WebMenuItemInvisibility.class);

        up.setUser(co(User.class).findByKey("USER_1"));
        save(new_(MenuSaveAction.class)
                .setInvisibleMenuItems(new HashSet<>(Arrays.asList(
                        "module1/module1item3",
                        "module2/module2group1",
                        "module2/module2group1/module2group1item1",
                        "module2/module2group1/module2group1item2",
                        "module2/module2item2",
                        "module2/module2group2/module2group2item2",
                        "module2/module2group2/module2group2item3"))));

        up.setUser(co(User.class).findByKey("USER_2"));
        final MenuProducer menuProducer = new MenuProducer(menuRetriever, mii, up);
        final Menu menu = menuProducer.newEntity();

        assertEquals("The menu has incorrect number of modules", 2, menu.getMenu().size());

        final List<Module> modules = menu.getMenu();
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

    @Override
    protected void populateDomain() {
        super.populateDomain();
        //Creating base and based on users
        final User user1 = save(new_(User.class, "USER_1").setBase(true).setActive(true).setEmail("user1@mail"));
        save(new_(User.class, "USER_2").setBase(false).setActive(true).setEmail("user2@mail").setBasedOnUser(user1));
    }
}
