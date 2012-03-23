package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController2;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController2;

/**
 * This is a class to persist configuration of an individual entity locator.
 * <p>
 * Property <code>key</code> should contain a string representation of a corresponding locator configuration class. The Class type could not be used as a key type because it is not
 * Comparable.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Entity locator configuration")
@DefaultController(IEntityLocatorConfigController.class)
@DefaultController2(IEntityLocatorConfigController2.class)
@MapEntityTo("ENTITY_LOCATOR_CONFIG")
public class EntityLocatorConfig extends AbstractConfiguration<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "Application user owning this configuration.")
    @MapTo("ID_CRAFT")
    private User owner;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Type", desc = "Master type.")
    @MapTo("LOCATOR_TYPE")
    private String locatorType;

    protected EntityLocatorConfig() {
	setKey(new DynamicEntityKey(this));
    }

    /**
     * A helper setter to convert entity locator to the string value.
     *
     * @param masterModelType
     */
    public void setLocatorModelType(final Class<?> masterModelType) {
	setLocatorType(PropertyTypeDeterminator.stripIfNeeded(masterModelType).getName());
    }

    /**
     * A helper getter to obtain entity locator type from string value.
     *
     * @return
     */
    public Class<?> getLocatorModelType() {
	try {
	    return Class.forName(getLocatorType());
	} catch (final ClassNotFoundException e) {
	    throw new IllegalStateException("Entity locator type '" + getLocatorType() + "' is not a valid class name.");
	}
    }

    public User getOwner() {
	return owner;
    }

    @Observable
    @NotNull
    @EntityExists(User.class)
    public void setOwner(final User owner) {
	if (owner != null && !owner.isBase()) {
	    throw new Result(this, new IllegalArgumentException("Only base users are allowed to be used for a base configuration."));
	}
	this.owner = owner;
    }

    public String getLocatorType() {
	return locatorType;
    }

    @Observable
    @NotNull
    public void setLocatorType(final String locatorType) {
	this.locatorType = locatorType;
    }

}
