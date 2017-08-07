package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
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
    
    @Observable
    public EntityWithMetaProperty setProp(final String prop) {
        this.prop = prop;
        return this;
    }
    
    public String getProp() {
        return prop;
    }
    
}
