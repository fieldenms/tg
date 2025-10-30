package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(ActivatableUnionOwnerCo.class)
public class ActivatableUnionOwner extends ActivatableAbstractEntity<String> {

    @IsProperty
    @MapTo
    private Union union;

    @IsProperty
    @MapTo
    private Member1 member1;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private Union union2;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = false)
    private Union union3;

    @IsProperty
    @MapTo
    @SkipActivatableTracking
    private Union union4;

    public Union getUnion4() {
        return union4;
    }

    @Observable
    public ActivatableUnionOwner setUnion4(final Union union4) {
        this.union4 = union4;
        return this;
    }

    public Union getUnion3() {
        return union3;
    }

    @Observable
    public ActivatableUnionOwner setUnion3(final Union union3) {
        this.union3 = union3;
        return this;
    }

    public Union getUnion2() {
        return union2;
    }

    @Observable
    public ActivatableUnionOwner setUnion2(final Union union2) {
        this.union2 = union2;
        return this;
    }

    public Member1 getMember1() {
        return member1;
    }

    @Observable
    public ActivatableUnionOwner setMember1(final Member1 member1) {
        this.member1 = member1;
        return this;
    }

    public Union getUnion() {
        return union;
    }

    @Observable
    public ActivatableUnionOwner setUnion(final Union union) {
        this.union = union;
        return this;
    }

    @Observable
    @Override
    public ActivatableUnionOwner setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
