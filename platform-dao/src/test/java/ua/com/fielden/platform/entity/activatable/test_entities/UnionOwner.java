package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(UnionOwnerCo.class)
public class UnionOwner extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Union union;

    public Union getUnion() {
        return union;
    }

    @Observable
    public UnionOwner setUnion(final Union union) {
        this.union = union;
        return this;
    }

}
