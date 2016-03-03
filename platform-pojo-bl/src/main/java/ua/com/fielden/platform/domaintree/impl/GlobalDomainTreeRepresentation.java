package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import java.util.HashSet;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;

/**
 * A global domain tree implementation.
 * 
 * @author TG Team
 * 
 */
public class GlobalDomainTreeRepresentation extends AbstractDomainTree implements IGlobalDomainTreeRepresentation {
    private final Logger logger = Logger.getLogger(getClass());
    private final EntityFactory factory;
    private final IUserProvider userProvider;
    private final IEntityLocatorConfigController elcController;
    private final DomainTreeVersionMaintainer versionMaintainer;

    public GlobalDomainTreeRepresentation(final ISerialiser serialiser, final ISerialiser0 serialiser0, final EntityFactory factory, final IUserProvider userProvider, final IEntityCentreConfigController entityCentreConfigController, final IEntityMasterConfigController entityMasterConfigController, final IEntityLocatorConfigController entityLocatorConfigController) {
        super(serialiser);
        this.factory = factory;
        this.userProvider = userProvider;
        this.elcController = entityLocatorConfigController;
        this.versionMaintainer = new DomainTreeVersionMaintainer(serialiser, serialiser0, elcController, entityCentreConfigController, entityMasterConfigController);
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer getLocatorManagerByDefault(final Class<?> propertyType) {
        AbstractDomainTree.validateRootType(propertyType);
        // only base users have default locators assigned:
        final User currentUser = userProvider.getUser();
        final User baseOfTheCurrentUser = currentUser.isBase() ? currentUser : currentUser.getBasedOnUser();

        final String propertyTypeName = generateKey(propertyType);
        if (elcController.entityWithKeyExists(baseOfTheCurrentUser, propertyTypeName)) { // the persistence layer contains a default locator for "propertyType", so it should be retrieved and deserialised
            final EntityLocatorConfig elc = elcController.findByKey(baseOfTheCurrentUser, propertyTypeName);
            try {
                return versionMaintainer.maintainLocatorVersion(elc); // getSerialiser().deserialise(elc.getConfigBody(), ILocatorDomainTreeManagerAndEnhancer.class);
            } catch (final Exception e) {
                e.printStackTrace();
                final String message = "Unable to deserialise a default locator instance for type [" + propertyType.getSimpleName() + "] for base user [" + baseOfTheCurrentUser
                        + "] of current user [" + currentUser + "].";
                logger.error(message);
                throw new IllegalStateException(message);
            }
        } else { // there is no default locator for "propertyType" -- return a brand new empty instance
            // configure a brand new instance with custom default logic if needed
            return new LocatorDomainTreeManagerAndEnhancer(getSerialiser(), new HashSet<Class<?>>() {
                {
                    add(propertyType);
                }
            });
        }
    }

    @Override
    public IGlobalDomainTreeRepresentation setLocatorManagerByDefault(final Class<?> propertyType, final ILocatorDomainTreeManagerAndEnhancer locatorManager) {
        AbstractDomainTree.validateRootType(propertyType);
        // only base users have default locators assigned:
        final User currentUser = userProvider.getUser();
        final User baseOfTheCurrentUser = currentUser.isBase() ? currentUser : currentUser.getBasedOnUser();

        final String propertyTypeName = generateKey(propertyType);
        final byte[] body = getSerialiser().serialise(locatorManager);
        final EntityLocatorConfig elc;

        if (elcController.entityWithKeyExists(baseOfTheCurrentUser, propertyTypeName)) { // the persistence layer contains a default locator for "propertyType"
            elc = elcController.findByKeyAndFetch(fetchOnly(EntityLocatorConfig.class).with("configBody").with("owner").with("locatorType"), baseOfTheCurrentUser, propertyTypeName);
        } else { // there is no default locator for "propertyType" -- save a brand new instance
            elc = factory.newByKey(EntityLocatorConfig.class, baseOfTheCurrentUser, propertyTypeName);
        }
        elc.setConfigBody(body);
        elcController.save(elc);
        return this;
    }

    protected DomainTreeVersionMaintainer versionMaintainer() {
        return versionMaintainer;
    }
}
