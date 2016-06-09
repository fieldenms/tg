package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;

/**
 *
 * This is a class to persist configuration of entity masters. At this stage the persistence context includes only configurations for entity locators.
 * <p>
 * Property <code>masterType</code> should contain a string representation of a corresponding master configuration class.
 *
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Entity master configuration")
@CompanionObject(IEntityMasterConfig.class)
@MapEntityTo("ENTITY_MASTER_CONFIG")
public class EntityMasterConfig extends AbstractConfiguration<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "Application user owning this configuration.")
    @MapTo("ID_CRAFT")
    private User owner;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Type", desc = "Master type.")
    @MapTo("MASTER_TYPE")
    private String masterType;

    protected EntityMasterConfig() {
        setKey(new DynamicEntityKey(this));
    }

    /**
     * A helper setter to convert master UI model to the string value.
     *
     * @param masterModelType
     */
    public void setMasterModelType(final Class<?> masterModelType) {
        setMasterType(PropertyTypeDeterminator.stripIfNeeded(masterModelType).getName());
    }

    /**
     * A helper getter to obtain master UI model type from a string value.
     *
     * @return
     */
    public Class<?> getMasterModelType() {
        try {
            return Class.forName(getMasterType());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Master type '" + getMasterType() + "' is not a valid class name.");
        }
    }

    public User getOwner() {
        return owner;
    }

    @Observable
    @EntityExists(User.class)
    public void setOwner(final User owner) {
        // TODO please redefine a rules for saving master configuration not only for base users, but for non-base too. Check the other places.
        //	if (owner != null && !owner.isBase()) {
        //	    throw new Result(this, new IllegalArgumentException("Only base users are allowed to be used for a base configuration."));
        //	}
        this.owner = owner;
    }

    public String getMasterType() {
        return masterType;
    }

    @Observable
    public void setMasterType(final String masterType) {
        this.masterType = masterType;
    }

}
