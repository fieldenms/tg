package ua.com.fielden.platform.entity.meta.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.meta.MetaPropertyReadonlyTestCase;
import ua.com.fielden.platform.error.Result;

/**
 * Entity that has editable-related logic, which is designed for test case {@link MetaPropertyReadonlyTestCase}.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityForReadOnlyTesting extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    public static final String NOT_EDITABLE_REASON = "This entity is not editable.";

    private boolean editable = true;

    @IsProperty
    @MapTo
    private Integer intProp;
    
    @IsProperty
    @MapTo
    @Readonly
    private Integer readonlyIntProp;

    @Observable
    public EntityForReadOnlyTesting setReadonlyIntProp(final Integer readonlyIntProp) {
        this.readonlyIntProp = readonlyIntProp;
        return this;
    }

    public Integer getReadonlyIntProp() {
        return readonlyIntProp;
    }
    
    @Observable
    public EntityForReadOnlyTesting setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Override
    public Result isEditable() {
        return editable ? Result.successful(this) : Result.failure(NOT_EDITABLE_REASON);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}