package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

@CompanionObject(TestExists_UnionCo.class)
public class TestExists_Union extends AbstractUnionEntity {

    @IsProperty
    @MapTo
    private TestExists_Member1 member1;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true)
    private TestExists_Member2 member2;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation
    private TestExists_Member3 member3;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private TestExists_Member4 member4;

    public TestExists_Member4 getMember4() {
        return member4;
    }

    @Observable
    public TestExists_Union setMember4(final TestExists_Member4 member4) {
        this.member4 = member4;
        return this;
    }

    public TestExists_Member3 getMember3() {
        return member3;
    }

    @Observable
    public TestExists_Union setMember3(final TestExists_Member3 member3) {
        this.member3 = member3;
        return this;
    }

    public TestExists_Member2 getMember2() {
        return member2;
    }

    @Observable
    public TestExists_Union setMember2(final TestExists_Member2 member2) {
        this.member2 = member2;
        return this;
    }

    public TestExists_Member1 getMember1() {
        return member1;
    }

    @Observable
    public TestExists_Union setMember1(final TestExists_Member1 member1) {
        this.member1 = member1;
        return this;
    }

}
