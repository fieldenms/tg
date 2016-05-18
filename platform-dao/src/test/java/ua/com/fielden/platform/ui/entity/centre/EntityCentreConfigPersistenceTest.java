package ua.com.fielden.platform.ui.entity.centre;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigPersistenceTest extends AbstractDaoTestCase {
    private final IEntityCentreConfig dao = getInstance(EntityCentreConfigDao.class);
    private final IMainMenuItemController menuDao = getInstance(MainMenuItemControllerDao.class);
    private final IUser userDao = getInstance(UserDao.class);

    @Test
    public void test_insertion_and_retrieval_of_binary_data() {
        final EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 1, 2, 3 });
        dao.save(config);

        final List<EntityCentreConfig> result = dao.getPage(0, 25).data();
        assertEquals("Incorrect number of retrieved configurations.", 1, result.size());
        assertTrue("Incorrectly saved binary property.", Arrays.equals(new byte[] { 1, 2, 3 }, result.get(0).getConfigBody()));
    }

    @Test
    public void test_update_of_binary_data() {
        EntityCentreConfig config = new_composite(EntityCentreConfig.class, userDao.findByKey("USER"), "CONFIG 1", menuDao.findByKey("type"));
        config.setConfigBody(new byte[] { 1, 2, 3 });
        dao.save(config);

        assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
        config.setConfigBody(new byte[] { 1, 2, 3, 4 });
        config = dao.save(config);
        assertEquals("Incorrect version.", Long.valueOf("1"), config.getVersion());

        final EntityCentreConfig fromDb = dao.findById(config.getId());

        assertNotNull("Configuration entity should exist.", fromDb);
        assertTrue("Incorrectly updated binary property.", Arrays.equals(new byte[] { 1, 2, 3, 4 }, fromDb.getConfigBody()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(User.class, "USER", "DESC").setBase(true).setPassword("PASSWD").setActive(true));
        save(new_(MainMenuItem.class, "type", "desc").setOrder(1));
    }

}