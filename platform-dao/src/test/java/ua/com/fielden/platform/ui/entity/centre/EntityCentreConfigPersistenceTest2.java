package ua.com.fielden.platform.ui.entity.centre;

import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.security.provider.UserController2;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController2;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao2;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigPersistenceTest2 extends DbDrivenTestCase2 {
    private final IEntityCentreConfigController2 dao = injector.getInstance(EntityCentreConfigControllerDao2.class);
    private final IMainMenuItemController2 menuDao = injector.getInstance(MainMenuItemControllerDao2.class);
    private final IUserDao2 userDao = injector.getInstance(UserController2.class);


    public void test_insertion_and_retrieval_of_binary_data() {
	final EntityCentreConfig config = entityFactory.newByKey(EntityCentreConfig.class, userDao.findById(0L), "CONFIG 1", menuDao.findById(0L));
	config.setConfigBody(new byte[]{1,2,3});
	dao.save(config);

	final List<EntityCentreConfig> result = dao.getPage(0, 25).data();
	assertEquals("Incorrect number of retrieved configurations.", 1, result.size());
	assertTrue("Incorrectly saved binary property.", Arrays.equals(new byte[]{1, 2, 3}, result.get(0).getConfigBody()));
    }

    public void test_update_of_binary_data() {
	final EntityCentreConfig config = entityFactory.newByKey(EntityCentreConfig.class, userDao.findById(0L), "CONFIG 1", menuDao.findById(0L));
	config.setConfigBody(new byte[]{1,2,3});
	dao.save(config);

	assertEquals("Incorrect version.", Long.valueOf("0"), config.getVersion());
	config.setConfigBody(new byte[]{1,2,3, 4});
	dao.save(config);
	assertEquals("Incorrect version.", Long.valueOf("1"), config.getVersion());

	final EntityCentreConfig fromDb = dao.findById(config.getId());

	assertNotNull("Configuration entity should exist.", fromDb);
	assertTrue("Incorrectly updated binary property.", Arrays.equals(new byte[]{1, 2, 3, 4}, fromDb.getConfigBody()));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] {"src/test/resources/data-files/entity-centre-config-test-case.flat.xml"};
    }
}