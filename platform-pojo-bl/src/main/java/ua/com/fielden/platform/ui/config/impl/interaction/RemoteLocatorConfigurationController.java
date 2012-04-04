package ua.com.fielden.platform.ui.config.impl.interaction;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.ClientSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


/**
 * Remote implementation of {@link ILocatorConfigurationController}.
 *
 * @author TG Team
 *
 */
public class RemoteLocatorConfigurationController implements ILocatorConfigurationController {

    private final IEntityLocatorConfigController lccController;
    private final ISerialiser serialiser;
    private final EntityFactory factory;
    private final IUserProvider userProvider;

    private static final char KEY_SEPARATOR = '\u2190';

    private final Map<String, EntityLocatorConfig> cach = new HashMap<String, EntityLocatorConfig>();

    @Inject
    protected RemoteLocatorConfigurationController(//
    final IEntityLocatorConfigController lccController,//
    final EntityFactory factory,//
    final IUserProvider userProvider) {
	this.lccController = lccController;
	this.serialiser = new ClientSerialiser(factory);
	this.factory = factory;
	this.userProvider = userProvider;
    }

    @Override
    public String generateKeyForDefaultAutocompleterConfiguration(final Class<?> forType) {
	return PropertyTypeDeterminator.stripIfNeeded(forType).getName();
    }

    @Override
    public String generateKeyForAutocompleterConfiguration(final Class<?> entityType, final String propertyName) {
	return generateKeyForDefaultAutocompleterConfiguration(entityType) + KEY_SEPARATOR + propertyName;
    }

    @Override
    public void save(final String key, final byte[] objectToSave) {
	if (!isDefaultLocator(key)) {
	    throw new IllegalArgumentException("Only default locators are supported.");
	}

	final User user = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();

	EntityLocatorConfig config = cach.get(key) != null ? cach.get(key) : lccController.findByKey(user, key);
	if (config != null) {
	    config.setConfigBody(objectToSave);
	    config = lccController.save(config);
	} else {
	    config = factory.newByKey(EntityLocatorConfig.class, user, key);
	    config.setConfigBody(objectToSave);
	    config = lccController.save(config);
	}
	cach.put(key, config);
    }

    @Override
    public byte[] load(final String key) {
	if (!isDefaultLocator(key)) {
	    throw new IllegalArgumentException("Only default locators are supported.");
	}

	final User user = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();

	final EntityLocatorConfig config = cach.get(key) != null ? cach.get(key) : lccController.findByKey(user, key);
	cach.put(key, config);

	return config != null ? config.getConfigBody() : null;
    }

    @Override
    public boolean exists(final String key) {
	if (!isDefaultLocator(key)) {
	    throw new IllegalArgumentException("Only default locators are supported.");
	}

	final User user = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();

	final EntityResultQueryModel<EntityLocatorConfig> model = select(EntityLocatorConfig.class).where().prop("owner").eq().val(user).and().prop("locatorType").eq().val(key).model();
	return lccController.count(model) > 0;
    }

    private boolean isDefaultLocator(final String key) {
	return key.indexOf(KEY_SEPARATOR) < 0;
    }

    @Override
    public ISerialiser getSerialiser() {
	return serialiser;
    }

    @Override
    public void removeConfiguration(final String key) {
	throw new UnsupportedOperationException("Removal of locator configurtions is not supported.");
    }

    @Override
    public Result canRemove(final String centerKey) {
	throw new UnsupportedOperationException("Removal of locator configurtions is not supported.");
    }

    @Override
    public Result canConfigureAnalysis(final String centerKey) {
	throw new UnsupportedOperationException("Unsupported operation (canConfigureAnalysis).");
    }

    @Override
    public Result canSave(final String locatorKey) {
	return userProvider.getUser().isBase() ? Result.successful(this) : new Result(this, new IllegalStateException("Only base users can modify locators."));
    }

    @Override
    public Result canConfigure(final String locatorKey) {
	return canSave(locatorKey);
    }
}