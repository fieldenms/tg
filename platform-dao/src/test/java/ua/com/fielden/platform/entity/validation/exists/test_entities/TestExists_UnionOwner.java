package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(TestExists_UnionOwnerCo.class)
public class TestExists_UnionOwner extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private TestExists_Union union1;

    public TestExists_Union getUnion1() {
        return union1;
    }

    @Observable
    public TestExists_UnionOwner setUnion1(final TestExists_Union union1) {
        this.union1 = union1;
        return this;
    }

}
