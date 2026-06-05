package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.validation.test_entities.definers.MakeProp1NotRequired;
import ua.com.fielden.platform.entity.validation.test_entities.definers.MakeProp1Required;

/// Entity with properties that change their requiredness dynamically.
///
@KeyType(String.class)
@CompanionObject(EntityWithDynamicRequirednessCo.class)
@MapEntityTo
public class EntityWithDynamicRequiredness extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Prop 1", desc = "With dynamic requiredness")
    private Integer prop1;

    @IsProperty
    @MapTo
    @Title(value = "Prop 2", desc = "Has dependent prop 1 and does NOT CHANGE its requiredness")
    @Dependent("prop1")
    private Integer prop2;

    @IsProperty
    @MapTo
    @Title(value = "Prop 3", desc = "Has dependent prop 1 and make it NOT REQUIRED")
    @Dependent("prop1")
    @AfterChange(MakeProp1NotRequired.class)
    private Integer prop3;

    @IsProperty
    @MapTo
    @Title(value = "Prop 4", desc = "Has dependent prop 1 and makes it REQUIRED")
    @Dependent("prop1")
    @AfterChange(MakeProp1Required.class)
    private Integer prop4;

    @IsProperty
    @MapTo
    @Title(value = "Prop 5", desc = "Boolean property for testing requiredness semantics in application to this single property.")
    private boolean prop5;
    
    @IsProperty
    @MapTo
    @Title(value = "Prop 6", desc = "Boolean property that is mutually exclusive with Prop 7 and Prop 8.")
    @AfterChange(EntityWithDynamicRequirednessEitherOrDefiner.class)
    private boolean prop6;
    
    @IsProperty
    @MapTo
    @Title(value = "Prop 7", desc = "Boolean property that is mutually exclusive with Prop 6 and Prop 8.")
    @AfterChange(EntityWithDynamicRequirednessEitherOrDefiner.class)
    private boolean prop7;
    
    @IsProperty
    @MapTo
    @Title(value = "Prop 8", desc = "Boolean property that is mutually exclusive with Prop 7 and Prop 8.")
    @AfterChange(EntityWithDynamicRequirednessEitherOrDefiner.class)
    private boolean prop8;

    @IsProperty
    @MapTo
    @Unique
    @Title("Unique boolean")
    private boolean uniqueBoolean;

    @IsProperty
    @MapTo
    @Unique
    @Title("Unique String")
    private String uniqueString;

    public String getUniqueString() {
        return uniqueString;
    }

    @Observable
    public EntityWithDynamicRequiredness setUniqueString(final String uniqueString) {
        this.uniqueString = uniqueString;
        return this;
    }

    public boolean getUniqueBoolean() {
        return uniqueBoolean;
    }

    @Observable
    public EntityWithDynamicRequiredness setUniqueBoolean(final boolean uniqueBoolean) {
        this.uniqueBoolean = uniqueBoolean;
        return this;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp8(final boolean prop8) {
        this.prop8 = prop8;
        return this;
    }

    public boolean isProp8() {
        return prop8;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp7(final boolean prop7) {
        this.prop7 = prop7;
        return this;
    }

    public boolean isProp7() {
        return prop7;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp6(final boolean prop6) {
        this.prop6 = prop6;
        return this;
    }

    public boolean isProp6() {
        return prop6;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp4(final Integer prop4) {
        this.prop4 = prop4;
        return this;
    }

    public Integer getProp4() {
        return prop4;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp3(final Integer prop3) {
        this.prop3 = prop3;
        return this;
    }

    public Integer getProp3() {
        return prop3;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp2(final Integer prop2) {
        this.prop2 = prop2;
        return this;
    }

    public Integer getProp2() {
        return prop2;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp1(final Integer prop1) {
        this.prop1 = prop1;
        return this;
    }

    public Integer getProp1() {
        return prop1;
    }

    @Observable
    public EntityWithDynamicRequiredness setProp5(final boolean prop5) {
        this.prop5 = prop5;
        return this;
    }

    public boolean isProp5() {
        return prop5;
    }

}