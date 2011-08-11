package ua.com.fielden.platform.entity.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;

@KeyType(String.class)
public class EntityWithDependentProperties extends AbstractEntity<String> {

    public int oneCount = 0;
    public int twoCount = 0;
    public int threeCount = 0;
    public int fourCount = 0;
    public int fiveCount = 0;

    @IsProperty
    @Dependent("two")
    private String one;

    @IsProperty
    @Dependent({ "one", "three" })
    private String two;

    @IsProperty
    @Dependent({ "one", "two" })
    private String three;

    @IsProperty
    @Dependent({"one", "five"})
    private String four;

    @IsProperty
    @Dependent("one")
    private String five;

    public String getOne() {
	return one;
    }

    @Observable
    @DomainValidation
    public void setOne(final String one) {
	this.one = one;
    }

    public String getTwo() {
	return two;
    }

    @Observable
    @DomainValidation
    public void setTwo(final String two) {
	this.two = two;
    }

    public String getThree() {
	return three;
    }

    @Observable
    @DomainValidation
    public void setThree(final String three) {
	this.three = three;
    }

    public String getFour() {
	return four;
    }

    @Observable
    @DomainValidation
    public void setFour(final String four) {
	this.four = four;
    }

    public String getFive() {
        return five;
    }

    @Observable
    @DomainValidation
    public void setFive(final String five) {
        this.five = five;
    }

}
