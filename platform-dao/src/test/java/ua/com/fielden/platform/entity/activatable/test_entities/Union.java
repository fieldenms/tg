package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

@CompanionObject(UnionCo.class)
public class Union extends AbstractUnionEntity {

    @IsProperty
    @MapTo
    private Member1 member1;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private Member2 member2;

    @IsProperty
    @MapTo
    private ActivatableUnionOwner owner;

    public ActivatableUnionOwner getOwner() {
        return owner;
    }

    @Observable
    public Union setOwner(final ActivatableUnionOwner owner) {
        this.owner = owner;
        return this;
    }

    public Member2 getMember2() {
        return member2;
    }

    @Observable
    public Union setMember2(final Member2 member2) {
        this.member2 = member2;
        return this;
    }

    public Member1 getMember1() {
        return member1;
    }

    @Observable
    public Union setMember1(final Member1 member1) {
        this.member1 = member1;
        return this;
    }

}
