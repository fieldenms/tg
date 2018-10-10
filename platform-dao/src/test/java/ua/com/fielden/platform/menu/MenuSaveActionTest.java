package ua.com.fielden.platform.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.EntityUtils;

public class MenuSaveActionTest extends AbstractDaoTestCase {


    @Test
    public void only_basse_user_can_save_module_menu_changes() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(co$(User.class).findByKey("USER_1"));

        save(new_(MenuSaveAction.class)
                .setInvisibleMenuItems(new HashSet<>(Arrays.asList("item1", "item2", "item3")))
                .setVisibleMenuItems(new HashSet<>(Arrays.asList("item2"))));

        final List<WebMenuItemInvisibility> menuItems = co$(WebMenuItemInvisibility.class).
                getAllEntities(from(select(WebMenuItemInvisibility.class).where().prop("owner").eq().val(up.getUser()).model()).
                        with(EntityUtils.fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model());
        assertEquals("The number of expected number of invisible menu items is incorrect", 2, menuItems.size());
        final Set<String> items = new HashSet<>(Arrays.asList("item1", "item3"));
        assertTrue("Menu items should contain item1 and item3", items.contains(menuItems.get(0).getMenuItemUri()) && items.contains(menuItems.get(1).getMenuItemUri()));
    }

    @Test
    public void non_base_user_can_not_save_module_menu_changes() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(co$(User.class).findByKey("USER_2"));

        save(new_(MenuSaveAction.class)
                .setInvisibleMenuItems(new HashSet<>(Arrays.asList("item1", "item2", "item3")))
                .setVisibleMenuItems(new HashSet<>(Arrays.asList("item2"))));

        final List<WebMenuItemInvisibility> menuItems = co$(WebMenuItemInvisibility.class).
                getAllEntities(from(select(WebMenuItemInvisibility.class).where().prop("owner").eq().val(up.getUser().getBasedOnUser()).model()).
                        with(EntityUtils.fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model());
        assertEquals("The number of expected number of invisible menu items is incorrect", 0, menuItems.size());
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();
        //Creating base and based on users
        final User user1 = save(new_(User.class, "USER_1").setBase(true).setActive(true).setEmail("user1@mail"));
        save(new_(User.class, "USER_2").setBase(false).setActive(true).setEmail("user2@mail").setBasedOnUser(user1));
    }
}
