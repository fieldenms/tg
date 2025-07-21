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

    @IsProperty
    @MapTo
    private Member1 member1;

    public Member1 getMember1() {
        return member1;
    }

    @Observable
    public UnionOwner setMember1(final Member1 member1) {
        this.member1 = member1;
        return this;
    }

    public Union getUnion() {
        return union;
    }

    @Observable
    public UnionOwner setUnion(final Union union) {
        this.union = union;
        return this;
    }

}
