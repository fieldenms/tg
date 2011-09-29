package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.domaintree.testing.TgKryo1;
import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeRepresentationTest extends DbDrivenTestCase {
    protected final ISerialiser serialiser = new TgKryo1(entityFactory, new ProvidedSerialisationClassProvider());
    protected final IUserDao userDao = injector.getInstance(IUserDao.class);
    protected final IUserProvider baseUserProvider = new IUserProvider() {
	@Override
	public User getUser() {
	    return userDao.findById(0L, new fetchAll<User>(User.class));
	}
    };
    protected final IUserProvider nonBaseUserProvider = new IUserProvider() {
	@Override
	public User getUser() {
	    return userDao.findById(1L, new fetchAll<User>(User.class));
	}
    };
    protected final IUserProvider nonBaseUserProvider2 = new IUserProvider() {
	@Override
	public User getUser() {
	    return userDao.findById(2L, new fetchAll<User>(User.class));
	}
    };
    protected final IMainMenuItemController mainMenuItemController = injector.getInstance(IMainMenuItemController.class);
    protected final IEntityCentreConfigController entityCentreConfigController = injector.getInstance(IEntityCentreConfigController.class);
    protected final IEntityMasterConfigController entityMasterConfigController = injector.getInstance(IEntityMasterConfigController.class);
    protected final IEntityLocatorConfigController entityLocatorConfigController = injector.getInstance(IEntityLocatorConfigController.class);

    public void test_that_default_locators_representation_works_for_non_base_user() {
	final IGlobalDomainTreeManager managerForNonBaseUser = new GlobalDomainTreeManager(serialiser, entityFactory, nonBaseUserProvider, mainMenuItemController, entityCentreConfigController, entityMasterConfigController, entityLocatorConfigController);
	default_locators_representation_test_for(managerForNonBaseUser);
    }

    public void test_that_default_locators_representation_works_for_base_user() {
	final IGlobalDomainTreeManager managerForBaseUser = new GlobalDomainTreeManager(serialiser, entityFactory, baseUserProvider, mainMenuItemController, entityCentreConfigController, entityMasterConfigController, entityLocatorConfigController);
	default_locators_representation_test_for(managerForBaseUser);
    }

    protected static void default_locators_representation_test_for(final IGlobalDomainTreeManager manager) {
	// for the first time the manager just produces a default instance of locator
	final ILocatorDomainTreeManagerAndEnhancer mgrAndEnhancer = manager.getGlobalRepresentation().getLocatorManagerByDefault(SlaveEntity.class);
	assertNotNull("Should not be null.", mgrAndEnhancer);
	assertTrue("The state of locator is incorrect.", mgrAndEnhancer.isRunAutomatically());

	// ensures that new instance will be produced every time
	assertTrue("Should be brand new instance every time.", manager.getGlobalRepresentation().getLocatorManagerByDefault(SlaveEntity.class) != mgrAndEnhancer);
	assertTrue("Should be brand new instance every time.", manager.getGlobalRepresentation().getLocatorManagerByDefault(SlaveEntity.class).equals(mgrAndEnhancer));

	// alter and save
	mgrAndEnhancer.setRunAutomatically(false);
	manager.getGlobalRepresentation().setLocatorManagerByDefault(SlaveEntity.class, mgrAndEnhancer);

	// after saving the manager just retrieves a default instance of locator
	final ILocatorDomainTreeManagerAndEnhancer newMgrAndEnhancer = manager.getGlobalRepresentation().getLocatorManagerByDefault(SlaveEntity.class);
	assertNotNull("Should not be null.", newMgrAndEnhancer);
	assertFalse("The state of locator is incorrect.", newMgrAndEnhancer.isRunAutomatically());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] {"src/test/resources/data-files/global-domain-tree-representation-test-case.flat.xml"};
    }
}