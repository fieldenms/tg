package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(TestExists_ActivatableUnionOwnerCo.class)
public class TestExists_ActivatableUnionOwner extends ActivatableAbstractEntity<String> {

    @IsProperty
    @MapTo
    private TestExists_Union union1;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private TestExists_Union union2;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation
    private TestExists_Union union3;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true)
    private TestExists_Union union4;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true, skipActiveOnly = true)
    private TestExists_Union union5;

    public TestExists_Union getUnion5() {
        return union5;
    }

    @Observable
    public TestExists_ActivatableUnionOwner setUnion5(final TestExists_Union union5) {
        this.union5 = union5;
        return this;
    }

    public TestExists_Union getUnion4() {
        return union4;
    }

    @Observable
    public TestExists_ActivatableUnionOwner setUnion4(final TestExists_Union union4) {
        this.union4 = union4;
        return this;
    }

    public TestExists_Union getUnion3() {
        return union3;
    }

    @Observable
    public TestExists_ActivatableUnionOwner setUnion3(final TestExists_Union union3) {
        this.union3 = union3;
        return this;
    }

    public TestExists_Union getUnion2() {
        return union2;
    }

    @Observable
    public TestExists_ActivatableUnionOwner setUnion2(final TestExists_Union union2) {
        this.union2 = union2;
        return this;
    }

    public TestExists_Union getUnion1() {
        return union1;
    }

    @Observable
    public TestExists_ActivatableUnionOwner setUnion1(final TestExists_Union union1) {
        this.union1 = union1;
        return this;
    }

    @Observable
    @Override
    public TestExists_ActivatableUnionOwner setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
