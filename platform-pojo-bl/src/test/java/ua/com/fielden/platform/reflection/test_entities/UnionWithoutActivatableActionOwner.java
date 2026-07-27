package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.activatable.test_entities.UnionWithoutActivatable;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(UnionWithoutActivatableActionOwnerCo.class)
public class UnionWithoutActivatableActionOwner extends AbstractFunctionalEntityWithCentreContext<String> {

    @IsProperty
    @Title
    private UnionWithoutActivatable union;

    public UnionWithoutActivatable getUnion() {
        return union;
    }

    @Observable
    public UnionWithoutActivatableActionOwner setUnion(final UnionWithoutActivatable union) {
        this.union = union;
        return this;
    }

}