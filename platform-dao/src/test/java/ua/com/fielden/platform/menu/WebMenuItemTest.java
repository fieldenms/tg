package ua.com.fielden.platform.menu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class WebMenuItemTest extends AbstractDaoTestCase {

    @Test
    public void only_base_user_can_be_used_for_web_menu_items() {
        final User owner = co(User.class).findByKey("USER_1");
        final User nonBaseOwner = co(User.class).findByKey("USER_2");

        final WebMenuItemInvisibility menuItemInvisibility = new_(WebMenuItemInvisibility.class).setMenuItemUri("item1").setOwner(owner);
        assertTrue("Menu item should be valid because owner is base user.", menuItemInvisibility.isValid().isSuccessful());

        final WebMenuItemInvisibility menuItemWithNonBaseUser = new_(WebMenuItemInvisibility.class).setMenuItemUri("item1").setOwner(nonBaseOwner);
        assertFalse("Menu item should be invalid because owner is not base user.", menuItemWithNonBaseUser.isValid().isSuccessful());

        final WebMenuItemInvisibility menuItemWithNullOwner = new_(WebMenuItemInvisibility.class).setMenuItemUri("item1").setOwner(null);
        assertFalse("Menu item should be invalid because owner is null.", menuItemWithNullOwner.isValid().isSuccessful());

    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        //Creating base and based on users
        final User user1 = save(new_(User.class, "USER_1").setBase(true).setActive(true).setEmail("user1@mail"));
        save(new_(User.class, "USER_2").setBase(false).setActive(true).setEmail("user2@mail").setBasedOnUser(user1));
    }
}
