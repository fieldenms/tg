package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.validation.test_entities.definers.MakeProp1NotRequired;
import ua.com.fielden.platform.entity.validation.test_entities.definers.MakeProp1Required;
/**
 * Entity with properties that change their requiredness dynamically.
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithDynamicRequiredness extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

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





}