package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0ForDomainTreesTestingPurposes;
import ua.com.fielden.platform.serialisation.api.impl.SerialiserForDomainTreesTestingPurposes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeRepresentationTest extends AbstractDaoTestCase {
    private final EntityFactory entityFactory = getInstance(EntityFactory.class);
    private final ISerialiser0 serialiser0 = new Serialiser0ForDomainTreesTestingPurposes(entityFactory, new ClassProviderForTestingPurposes());
    private final ISerialiser serialiser = new SerialiserForDomainTreesTestingPurposes(entityFactory, new ClassProviderForTestingPurposes());
    private final IUser userDao = getInstance(IUser.class);

    protected GlobalDomainTreeManager createManagerForNonBaseUser2() {
        return createGlobalDomainTreeManager("USER3");
    }

    protected GlobalDomainTreeManager createManagerForNonBaseUser() {
        return createGlobalDomainTreeManager("USER2");
    }

    protected GlobalDomainTreeManager createManagerForBaseUser() {
        return createGlobalDomainTreeManager("USER1");
    }

    private GlobalDomainTreeManager createGlobalDomainTreeManager(final String userName) {
        return new GlobalDomainTreeManager(serialiser, serialiser0, entityFactory, createUserProvider(userName), getInstance(IMainMenuItemController.class), getInstance(IEntityCentreConfig.class), getInstance(IEntityCentreAnalysisConfig.class), getInstance(IEntityMasterConfig.class), getInstance(IEntityLocatorConfig.class)) {
            @Override
            protected void validateMenuItemType(final Class<?> menuItemType) { // no menu item validation due to non-existence of MiWithConfigurationSupport at platform-dao (it exists only on platform-ui level)
            }
        };
    }

    private IUserProvider createUserProvider(final String userName) {
        final IUserProvider baseUserProvider = new IUserProvider() {
            @Override
            public User getUser() {
                return userDao.findByKeyAndFetch(fetchAll(User.class), userName);
            }

            @Override
            public void setUsername(final String username, final IUser coUser) {
            }

            @Override
            public void setUser(User user) {
                
            }
        };
        return baseUserProvider;
    }

    @Test
    public void test_that_default_locators_representation_works_for_non_base_user() {
        default_locators_representation_test_for(createManagerForNonBaseUser());
    }

    @Test
    public void test_that_default_locators_representation_works_for_base_user() {
        default_locators_representation_test_for(createManagerForBaseUser());
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

    protected ILocatorDomainTreeManagerAndEnhancer initDefaultLocatorForSomeTestType(final IGlobalDomainTreeManager managerForNonBaseUser) {
        // initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
        final ILocatorDomainTreeManagerAndEnhancer ldtmae = new LocatorDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>() {
            {
                add(EntityWithStringKeyType.class);
            }
        });
        ldtmae.getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
        managerForNonBaseUser.getGlobalRepresentation().setLocatorManagerByDefault(EntityWithStringKeyType.class, ldtmae);
        assertFalse("Should not be the same instance, it should be retrived every time.", ldtmae == managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class));
        assertTrue("Should be equal instance, because no one has been changed default locator.", ldtmae.equals(managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class)));
        return ldtmae;
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformDomainTypes.types;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        final User baseUser1 = save(new_(User.class, "USER1").setBase(true));
        save(new_(User.class, "USER2").setBase(false).setBasedOnUser(baseUser1));
        save(new_(User.class, "USER3").setBase(false).setBasedOnUser(baseUser1));
        save(new_(MainMenuItem.class, "ua.com.fielden.platform.domaintree.testing.MiMasterEntityForGlobalDomainTree").setOrder(1));
    }
}