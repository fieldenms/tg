package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

@CompanionObject(UnionWithoutActivatableCo.class)
public class UnionWithoutActivatable extends AbstractUnionEntity {

    @IsProperty
    @MapTo
    private Member3 member3;

    @IsProperty
    @MapTo
    private Member6 member6;

    public Member6 getMember6() {
        return member6;
    }

    @Observable
    public UnionWithoutActivatable setMember6(final Member6 member6) {
        this.member6 = member6;
        return this;
    }

    public Member3 getMember3() {
        return member3;
    }

    @Observable
    public UnionWithoutActivatable setMember3(final Member3 member3) {
        this.member3 = member3;
        return this;
    }

}
