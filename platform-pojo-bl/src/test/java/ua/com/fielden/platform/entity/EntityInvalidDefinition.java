package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.Final;

/**
 * A non-persistent entity that represents an invalid definition due to properties annotated with <code>@Final(persistentOnly = true)</code>. 
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityInvalidDefinition extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Final(persistentOnly = true)
    private Integer firstProperty = null;
    
    public Integer getFirstProperty() {
        return firstProperty;
    }

    @Observable
    public EntityInvalidDefinition setFirstProperty(final Integer property) {
        this.firstProperty = property;
        return this;
    }
}