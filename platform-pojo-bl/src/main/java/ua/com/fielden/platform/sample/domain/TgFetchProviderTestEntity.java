package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.security.user.User;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgFetchProviderTestEntity.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgFetchProviderTestEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property", desc = "Property")
    private TgPersistentEntityWithProperties property;

    @IsProperty
    @MapTo
    @Title(value = "Additional property", desc = "Additional property")
    private User additionalProperty;
    
    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Prop For Validation", desc = "Crit-only single prop for centre validation test")
    private TgPersistentEntityWithProperties propForValidation;
    
    @Observable
    public TgFetchProviderTestEntity setPropForValidation(final TgPersistentEntityWithProperties propForValidation) {
        this.propForValidation = propForValidation;
        return this;
    }

    public TgPersistentEntityWithProperties getPropForValidation() {
        return propForValidation;
    }

    @Observable
    public TgFetchProviderTestEntity setAdditionalProperty(final User additionalProperty) {
        this.additionalProperty = additionalProperty;
        return this;
    }

    public User getAdditionalProperty() {
        return additionalProperty;
    }

    @Observable
    public TgFetchProviderTestEntity setProperty(final TgPersistentEntityWithProperties property) {
        this.property = property;
        return this;
    }

    public TgPersistentEntityWithProperties getProperty() {
        return property;
    }

}