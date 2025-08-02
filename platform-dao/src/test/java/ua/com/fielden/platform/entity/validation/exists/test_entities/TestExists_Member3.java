package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(TestExists_Member3Co.class)
public class TestExists_Member3 extends ActivatableAbstractEntity<String> {

    @IsProperty
    @MapTo
    private String str1;

    public String getStr1() {
        return str1;
    }

    @Observable
    public TestExists_Member3 setStr1(final String str1) {
        this.str1 = str1;
        return this;
    }

    @Observable
    @Override
    public TestExists_Member3 setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
