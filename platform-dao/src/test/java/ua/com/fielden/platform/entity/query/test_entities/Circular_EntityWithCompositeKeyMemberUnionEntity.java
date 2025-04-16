package ua.com.fielden.platform.entity.query.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(DynamicEntityKey.class)
public class Circular_EntityWithCompositeKeyMemberUnionEntity extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Circular_UnionEntity union;

    @Observable
    public Circular_EntityWithCompositeKeyMemberUnionEntity setUnion(final Circular_UnionEntity union) {
        this.union = union;
        return this;
    }

    public Circular_UnionEntity getUnion() {
        return union;
    }

}
