package ua.com.fielden.platform.entity.query.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.sample.domain.UnionEntity;

@MapEntityTo
@KeyType(DynamicEntityKey.class)
public class Circular_EntityWithCompositeKeyMemberUnionEntity extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private UnionEntity union;

    @Observable
    public Circular_EntityWithCompositeKeyMemberUnionEntity setUnion(final UnionEntity union) {
        this.union = union;
        return this;
    }

    public UnionEntity get() {
        return union;
    }

}
