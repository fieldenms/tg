package ua.com.fielden.platform.ui.config.impl.interaction;

import static ua.com.fielden.platform.equery.equery.select;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.ClientSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.interaction.IMasterConfigurationController;

import com.google.inject.Inject;

/**
 * Remote implementation of {@link IMasterConfigurationController}.
 * 
 * @author TG Team
 * 
 */
public class RemoteMasterConfigurationController implements IMasterConfigurationController {

    private final IEntityMasterConfigController mccController;
    private final ISerialiser serialiser;
    private final EntityFactory factory;
    private final IUserProvider userProvider;

    private final Map<String, EntityMasterConfig> cach = new HashMap<String, EntityMasterConfig>();

    @Inject
    protected RemoteMasterConfigurationController(//
    final IEntityMasterConfigController mccController,//
    final EntityFactory factory,//
    final IUserProvider userProvider) {
	this.mccController = mccController;
	this.serialiser = new ClientSerialiser(factory);
	this.factory = factory;
	this.userProvider = userProvider;
    }

    @Override
    public String generateKeyForMasterConfiguration(final Class<?> forType) {
	return PropertyTypeDeterminator.stripIfNeeded(forType).getName();
    }

    @Override
    public void save(final String key, final byte[] objectToSave) {
	final User user = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();

	EntityMasterConfig config = cach.get(key) != null ? cach.get(key) : mccController.findByKey(user, key);
	if (config != null) {
	    config.setConfigBody(objectToSave);
	    config = mccController.save(config);
	} else {
	    config = factory.newByKey(EntityMasterConfig.class, user, key);
	    config.setConfigBody(objectToSave);
	    config = mccController.save(config);
	}
	cach.put(key, config);
    }

    @Override
    public byte[] load(final String key) {
	final User user = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();
	final EntityMasterConfig config = cach.get(key) != null ? cach.get(key) : mccController.findByKey(user, key);
	cach.put(key, config);
	return config != null ? config.getConfigBody() : null;
    }

    @Override
    public boolean exists(final String key) {
	final User owner = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();

	final IQueryModel<EntityMasterConfig> model = select(EntityMasterConfig.class).where().prop("owner").eq().val(owner)//
	.and().prop("masterType").eq().val(key).model();
	return mccController.count(model) > 0;
    }

    @Override
    public ISerialiser getSerialiser() {
	return serialiser;
    }

    @Override
    public void removeConfiguration(final String key) {
	throw new UnsupportedOperationException("Master controller does not support this operation.");
    }

    @Override
    public Result canRemove(final String centerKey) {
	throw new UnsupportedOperationException("Master controller does not support this operation.");
    }

    @Override
    public Result canConfigureAnalysis(final String centerKey) {
	throw new UnsupportedOperationException("Master controller does not support this operation.");
    }

    @Override
    public Result canSave(final String masterKey) {
	return userProvider.getUser().isBase() ? Result.successful(this) : new Result(this, new IllegalStateException("Only base users can modify masters."));
    }

    @Override
    public Result canConfigure(final String masterKey) {
	return canSave(masterKey);
    }

}
