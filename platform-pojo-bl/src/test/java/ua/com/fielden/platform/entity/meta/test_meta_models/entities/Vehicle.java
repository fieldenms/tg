package ua.com.fielden.platform.entity.meta.test_meta_models.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesFive;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesFour;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesOne;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesThree;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesTwo;

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
