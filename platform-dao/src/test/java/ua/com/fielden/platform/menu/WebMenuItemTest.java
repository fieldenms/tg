package ua.com.fielden.platform.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.menu.validators.UserAsConfigurationOwnerValidator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class WebMenuItemTest extends AbstractDaoTestCase {

    @Test
    public void only_non_base_user_can_be_an_owner_of_web_menu_item_visibility_configuration() {
        final User owner = co$(User.class).findByKey("USER_1");
        final User nonBaseOwner = co$(User.class).findByKey("USER_2");

        final WebMenuItemInvisibility menuItem = new_(WebMenuItemInvisibility.class).setMenuItemUri("item1");
        menuItem.setOwner(owner);
        assertFalse(menuItem.isValid().isSuccessful());
        assertEquals(String.format(UserAsConfigurationOwnerValidator.USER_S_IS_A_BASE_USER_ERROR, owner), menuItem.isValid().getMessage());

        menuItem.setOwner(nonBaseOwner);
        assertTrue(menuItem.isValid().isSuccessful());

        co$(WebMenuItemInvisibility.class).save(menuItem);
    }

    @Test
    public void every_developer_needs_to_know_that_required_and_key_member_properties_cannot_be_assigned_to_null_thus_no_need_check_for_nullness_in_BCE_handlers_for_such_properties() {
        final User owner = co(User.class).findByKeyAndFetch(co(User.class).getFetchProvider().fetchModel(), "USER_2");

        final WebMenuItemInvisibility menuItem = new_(WebMenuItemInvisibility.class).setMenuItemUri("item1").setOwner(owner);
        assertTrue(menuItem.isValid().isSuccessful());

        menuItem.setOwner(null);
        assertFalse(menuItem.isValid().isSuccessful());
        assertEquals("Required property [User] is not specified for entity [Web Menu Item Invisibility].", menuItem.isValid().getMessage());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        //Creating base and based on users
        final User user1 = save(new_(User.class, "USER_1").setBase(true).setActive(true).setEmail("user1@mail"));
        save(new_(User.class, "USER_2").setBase(false).setActive(true).setEmail("user2@mail").setBasedOnUser(user1));
    }
}
