package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(UnionWithoutActivatableOwnerCo.class)
public class UnionWithoutActivatableOwner extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private UnionWithoutActivatable union;

    public UnionWithoutActivatable getUnion() {
        return union;
    }

    @Observable
    public UnionWithoutActivatableOwner setUnion(final UnionWithoutActivatable union) {
        this.union = union;
        return this;
    }

}
