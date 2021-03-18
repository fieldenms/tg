package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.menu.validators.UserAsConfigurationOwnerValidator;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;

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
@CompanionObject(IEntityLocatorConfig.class)
@MapEntityTo("ENTITY_LOCATOR_CONFIG")
@DenyIntrospection
public class EntityLocatorConfig extends AbstractConfiguration<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "Application user owning this configuration.")
    @MapTo("ID_CRAFT")
    @BeforeChange(@Handler(UserAsConfigurationOwnerValidator.class))
    private User owner;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Type", desc = "Master type.")
    @MapTo("LOCATOR_TYPE")
    private String locatorType;

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
    public void setOwner(final User owner) {
        this.owner = owner;
    }

    public String getLocatorType() {
        return locatorType;
    }

    @Observable
    public void setLocatorType(final String locatorType) {
        this.locatorType = locatorType;
    }

}
