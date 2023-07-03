package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(EntityWithUnionEntityWithSkipExistsValidationCo.class)
public class EntityWithUnionEntityWithSkipExistsValidation extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true)
    private UnionEntityWithSkipExistsValidation union;

    @Observable
    public EntityWithUnionEntityWithSkipExistsValidation setUnion(final UnionEntityWithSkipExistsValidation union) {
        this.union = union;
        return this;
    }

    public UnionEntityWithSkipExistsValidation getUnion() {
        return union;
    }

}