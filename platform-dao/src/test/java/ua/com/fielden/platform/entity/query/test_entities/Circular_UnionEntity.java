package ua.com.fielden.platform.entity.query.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

public class Circular_UnionEntity extends AbstractUnionEntity  {

    @IsProperty
    @MapTo
    private Circular_EntityWithCompositeKeyMemberUnionEntity entity;

    public Circular_EntityWithCompositeKeyMemberUnionEntity getEntity() {
        return entity;
    }

    @Observable
    public Circular_UnionEntity setEntity(final Circular_EntityWithCompositeKeyMemberUnionEntity entity) {
        this.entity = entity;
        return this;
    }

}
