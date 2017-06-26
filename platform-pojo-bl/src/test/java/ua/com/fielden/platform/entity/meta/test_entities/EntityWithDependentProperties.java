package ua.com.fielden.platform.entity.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesFive;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesFour;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesOne;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesThree;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesTwo;

@KeyType(String.class)
public class EntityWithDependentProperties extends AbstractEntity<String> {

    public int oneCount = 0;
    public int twoCount = 0;
    public int threeCount = 0;
    public int fourCount = 0;
    public int fiveCount = 0;

    @IsProperty
    @Dependent("two")
    @BeforeChange(@Handler(EntityWithDependentPropertiesOne.class))
    private String one;

    @IsProperty
    @Dependent({ "one", "three" })
    @BeforeChange(@Handler(EntityWithDependentPropertiesTwo.class))
    private String two;

    @IsProperty
    @Dependent({ "one", "two" })
    @BeforeChange(@Handler(EntityWithDependentPropertiesThree.class))
    private String three;

    @IsProperty
    @Dependent({ "one", "five" })
    @BeforeChange(@Handler(EntityWithDependentPropertiesFour.class))
    private String four;

    @IsProperty
    @Dependent("one")
    @BeforeChange(@Handler(EntityWithDependentPropertiesFive.class))
    private String five;

    public String getOne() {
        return one;
    }

    @Observable
    public void setOne(final String one) {
        this.one = one;
    }

    public String getTwo() {
        return two;
    }

    @Observable
    public void setTwo(final String two) {
        this.two = two;
    }

    public String getThree() {
        return three;
    }

    @Observable
    public void setThree(final String three) {
        this.three = three;
    }

    public String getFour() {
        return four;
    }

    @Observable
    public void setFour(final String four) {
        this.four = four;
    }

    public String getFive() {
        return five;
    }

    @Observable
    public void setFive(final String five) {
        this.five = five;
    }

}
