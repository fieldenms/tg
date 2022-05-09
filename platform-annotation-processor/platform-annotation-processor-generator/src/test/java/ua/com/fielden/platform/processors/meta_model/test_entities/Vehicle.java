package ua.com.fielden.platform.processors.meta_model.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
public class Vehicle extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Property name that is error prone and long", desc = "This is one of those properties.")
    private String propertyNameThatIsErrorProneAndLong;
    
    @IsProperty
    @MapTo
    @Title(value = "Insurance", desc = "The insurance for this vehicle.")
    private Insurance insurance;

    @Observable
    public Vehicle setInsurance(final Insurance insurance) {
        this.insurance = insurance;
        return this;
    }

    public Insurance getInsurance() {
        return insurance;
    }

    @Observable
    public Vehicle setGetPropertyNameThatIsErrorProneAndLong(final String color) {
        this.propertyNameThatIsErrorProneAndLong = color;
        return this;
    }

    public String getPropertyNameThatIsErrorProneAndLong() {
        return propertyNameThatIsErrorProneAndLong;
    }
}
