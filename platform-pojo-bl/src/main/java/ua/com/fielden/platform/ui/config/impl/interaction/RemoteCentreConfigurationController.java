package ua.com.fielden.platform.ui.config.impl.interaction;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.ClientSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Remote implementation of {@link ICenterConfigurationController}.
 * <p>
 * Unlike many controllers that have no context, this one should be instantiated for every menu item, where menu item is the context for this controller.
 *
 * @author TG Team
 *
 */
public class RemoteCentreConfigurationController implements ICenterConfigurationController {

    private final IEntityCentreConfigController eccController;
    private final ISerialiser serialiser;
    private final MainMenuItem principleMenuItem;
    private final IUserProvider userProvider;

    public static final char KEY_SEPARATOR = '\u2190';

    public RemoteCentreConfigurationController(//
    final IEntityCentreConfigController eccController,//
    final MainMenuItem principleMenuItem,//
    final IUserProvider userProvider) {
	this.eccController = eccController;
	this.serialiser = new ClientSerialiser(principleMenuItem.getEntityFactory());
	this.principleMenuItem = principleMenuItem;
	this.userProvider = userProvider;
    }

    @Override
    public String generateKeyForPrincipleCenter(final Class<?> forType) {
	if (!principleMenuItem.getKey().equals(forType.getName())) {
	    throw new IllegalArgumentException("Key " + forType.getName() + " does not match the context of this controller (" + principleMenuItem.getKey() + ")");
	}
	return principleMenuItem.getKey();
    }

    @Override
    public String generateKeyForNonPrincipleCenter(final String principleCenterKey, final String nonPrincipleCenterName) {
	return principleCenterKey + KEY_SEPARATOR + nonPrincipleCenterName;
    }

    @Override
    public List<String> getNonPrincipleCenters(final String principleCenterKey) {
	if (!principleCenterKey.equals(principleMenuItem.getKey())) {
	    throw new IllegalArgumentException("Key " + principleCenterKey + " does not match the context of this controller (" + principleMenuItem.getKey() + ")");
	}

	final List<String> result = new ArrayList<String>();
	for (final MainMenuItem item : principleMenuItem.getChildren()) {
	    if (!item.isPrincipal()) {
		result.add(item.getTitle());
	    }
	}
	return result;
    }

    @Override
    public boolean isNonPrincipleCenterNameValid(final String principleCenterKey, final String nonPrincipleCenterName) {
	return nonPrincipleCenterName.indexOf(KEY_SEPARATOR) < 0;
    }

    @Override
    public void save(final String key, final byte[] objectToSave) {
	final MainMenuItem item = getMenuItem(key);
	EntityCentreConfig config = item != null ? item.getConfig() : null;
	if (config != null) {
	    config.setConfigBody(objectToSave);
	    config = eccController.save(config);
	} else {
	    final String title = principleMenuItem.getKey().equals(key) ? key : key.substring(key.indexOf(KEY_SEPARATOR) + 1);
	    final EntityCentreConfig newConfig = principleMenuItem.getEntityFactory().newByKey(EntityCentreConfig.class, userProvider.getUser(), title, principleMenuItem);
	    newConfig.setConfigBody(objectToSave);
	    newConfig.setPrincipal(principleMenuItem.getKey().equals(key));
	    config = eccController.save(newConfig);
	}

	if (item != null) {
	    item.setConfig(config);
	} else {
	    final MainMenuItem newChild = new MainMenuItem();
	    newChild.setVisible(true); // correct visibility cannot be determined here
	    newChild.setKey(principleMenuItem.getKey());
	    newChild.setTitle(config.getTitle());
	    newChild.setParent(principleMenuItem);
	    newChild.setPrincipal(false);
	    newChild.setOrder(principleMenuItem.getOrder());
	    newChild.setConfig(config);
	    principleMenuItem.addChild(newChild);
	}
    }

    @Override
    public byte[] load(final String key) {
	final MainMenuItem item = getMenuItem(key);
	return item != null && item.getConfig() != null ? item.getConfig().getConfigBody() : null;
    }

    @Override
    public ISerialiser getSerialiser() {
	return serialiser;
    }

    @Override
    public boolean exists(final String key) {
	return getMenuItem(key) != null && getMenuItem(key).getConfig() != null;
    }

    private MainMenuItem getMenuItem(final String centreKey) {
	if (principleMenuItem.getKey().equals(centreKey)) {
	    if (principleMenuItem.getConfig() == null) {
		final User user = userProvider.getUser().isBase() ? userProvider.getUser() : userProvider.getUser().getBasedOnUser();
		final EntityCentreConfig config = eccController.findByKey(user, principleMenuItem.getTitle(), principleMenuItem);
		principleMenuItem.setConfig(config);
	    }
	    return principleMenuItem;
	}
	for (final MainMenuItem child : principleMenuItem.getChildren()) {
	    final String childKey = generateKeyForNonPrincipleCenter(principleMenuItem.getKey(), child.getTitle());
	    if (childKey.equals(centreKey)) {
		if (child.getConfig() == null) {
		    final User user = userProvider.getUser();
		    final Long[] ids = user.isBase() ? new Long[] { user.getId() } : new Long[] { user.getId(), user.getBasedOnUser().getId() };
		    child.setConfig(eccController.getEntity(from(findConfigByUser(child.getTitle(), ids)).build()));
		}
		return child;
	    }
	}
	return null;
    }

    @Override
    public void removeConfiguration(final String key) {
	if (principleMenuItem.getKey().equals(key)) {
	    throw new IllegalArgumentException("Principle menu item cannot be removed.");
	}

	final String title = key.substring(key.indexOf(KEY_SEPARATOR) + 1);
	final MainMenuItem item = getMenuItem(key);
	item.getParent().removeChild(item);
	eccController.delete(findConfigByUser(title, userProvider.getUser().getId()));
    }

    private EntityResultQueryModel<EntityCentreConfig> findConfigByUser(final String title, final Long... ids) {
	return select(EntityCentreConfig.class).where()//
	.prop("owner").in().values(ids)//
	.and().prop("title").eq().val(title)//
	.and().prop("menuItem").eq().val(principleMenuItem)//
	.and().prop("principal").eq().val(false).model();
    }

    @Override
    public Result canRemove(final String centreKey) {
	if (principleMenuItem.getKey().equals(centreKey)) {
	    return new Result(this, new IllegalArgumentException("Configuration for principle menu item cannot be deleted."));
	}

	final MainMenuItem child = findChildByKey(centreKey);

	if (child == null) {
	    return new Result(this, new IllegalArgumentException("There are no items with this key."));
	}

	if (!child.getConfig().getOwner().equals(userProvider.getUser())) {
	    return new Result(this, new IllegalArgumentException("The currently logged in user has no permission to remove this item."));
	}

	return Result.successful(this);
    }

    private MainMenuItem findChildByKey(final String key) {
	for (final MainMenuItem child : principleMenuItem.getChildren()) {
	    final String childKey = generateKeyForNonPrincipleCenter(principleMenuItem.getKey(), child.getTitle());
	    if (childKey.equals(key)) {
		return child;
	    }
	}
	return null;
    }

    @Override
    public Result canConfigureAnalysis(final String centreKey) {
	return canSave(centreKey);
    }

    @Override
    public Result canSave(final String centreKey) {
	if (principleMenuItem.getKey().equals(centreKey) && !userProvider.getUser().isBase()) {
	    return new Result(this, new IllegalArgumentException("The currently logged in user has no permission to change principle items."));
	}

	if (principleMenuItem.getKey().equals(centreKey)) {
	    if (userProvider.getUser().isBase()) {
		return Result.successful(this);
	    } else {
		new Result(this, new IllegalArgumentException("The currently logged in user has no permission to change principle items."));
	    }
	}

	final MainMenuItem child = findChildByKey(centreKey);

	if (child == null) {
	    return new Result(this, new IllegalArgumentException("There are no items with this key."));
	}

	if (!child.getConfig().getOwner().equals(userProvider.getUser())) {
	    return new Result(this, new IllegalArgumentException("The currently logged in user has no permission to remove this item."));
	}

	return Result.successful(this);
    }

    @Override
    public Result canConfigure(final String centreKey) {
	return canSave(centreKey);
    }

    @Override
    public Result canAddAnalysis(final String centreKey) {
	return canSave(centreKey);
    }

    @Override
    public Result canRemoveAnalysis(final String centreKey) {
	return canSave(centreKey);
    }
}
