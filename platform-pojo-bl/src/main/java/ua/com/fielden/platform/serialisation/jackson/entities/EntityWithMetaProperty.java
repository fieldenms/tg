package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;

/**
 * Entity class used for meta-property serialisation testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@DescTitle("Desc")
public class EntityWithMetaProperty extends AbstractEntity<String> {
    
    @IsProperty
    @MapTo
    @Title("Prop")
    @BeforeChange(@Handler(EntityWithMetaPropertyPropValidator.class))
    private String prop;
    
    @IsProperty
    @MapTo
    @Title("Required Prop")
    @Required // required by definition
    @CritOnly // crit-only to be able to make non-required
    private String requiredProp;
    
    @IsProperty
    @MapTo
    @Title("Non-editable Prop")
    @Readonly // non-editable by definition
    private String nonEditableProp;
    
    @IsProperty
    @MapTo
    @Title("Invisible Prop")
    @Invisible // its value is not available to clients
    private String invisibleProp;

    @IsProperty
    @MapTo
    @Title("Invisible In Centres Prop")
    @Invisible(centreOnly = true) // excluded from entity centres only
    private String invisibleInCentresProp;
    
    @IsProperty
    @MapTo
    @Title("Prop With Value Change Count")
    private String propWithValueChangeCount;
    
    @Observable
    public EntityWithMetaProperty setPropWithValueChangeCount(final String propWithValueChangeCount) {
        this.propWithValueChangeCount = propWithValueChangeCount;
        return this;
    }
    
    public String getPropWithValueChangeCount() {
        return propWithValueChangeCount;
    }
    
    @Observable
    public EntityWithMetaProperty setInvisibleProp(final String invisibleProp) {
        this.invisibleProp = invisibleProp;
        return this;
    }

    public String getInvisibleProp() {
        return invisibleProp;
    }

    @Observable
    public EntityWithMetaProperty setInvisibleInCentresProp(final String invisibleInCentresProp) {
        this.invisibleInCentresProp = invisibleInCentresProp;
        return this;
    }

    public String getInvisibleInCentresProp() {
        return invisibleInCentresProp;
    }
    
    @Observable
    public EntityWithMetaProperty setNonEditableProp(final String nonEditableProp) {
        this.nonEditableProp = nonEditableProp;
        return this;
    }
    
    public String getNonEditableProp() {
        return nonEditableProp;
    }
    
    @Observable
    public EntityWithMetaProperty setRequiredProp(final String requiredProp) {
        this.requiredProp = requiredProp;
        return this;
    }
    
    public String getRequiredProp() {
        return requiredProp;
    }
    
    @Observable
    public EntityWithMetaProperty setProp(final String prop) {
        this.prop = prop;
        return this;
    }
    
    public String getProp() {
        return prop;
    }
    
}
