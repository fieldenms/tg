package ua.com.fielden.platform.entity.before_change_event_handling;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.after_change_event_handling.AfterChangeEventHandler;
import ua.com.fielden.platform.entity.after_change_event_handling.InvalidAfterChangeEventHandler;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateTimeParam;
import ua.com.fielden.platform.entity.annotation.mutator.DblParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.annotation.mutator.MoneyParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.validation.NotNullValidator;

/**
 * Entity for the purpose of BCE handling tests.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class Entity extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Description")
    @BeforeChange({
            @Handler(value = NotNullValidator.class, str = { @StrParam(name = "validationMsg", value = "Property cannot be null.") }),
            @Handler(value = BeforeChangeEventHandler.class, integer = { @IntParam(name = "intParam1", value = 1), @IntParam(name = "intParam2", value = 12) }, str = { @StrParam(name = "strParam", value = "string value") }, dbl = { @DblParam(name = "dblParam", value = 0.65) }, date = { @DateParam(name = "dateParam", value = "2011-12-01 00:00:00") }, date_time = { @DateTimeParam(name = "dateTimeParam", value = "2011-12-01 00:00:00") }, money = { @MoneyParam(name = "moneyParam", value = "12.36") }, non_ordinary = { @ClassParam(name = "controllerParam", value = Controller.class) }, clazz = { @ClassParam(name = "classParam", value = String.class) }),
            @Handler(InvalidBeforeChangeEventHandler.class) })
    @AfterChange(value = AfterChangeEventHandler.class, integer = { @IntParam(name = "intParam1", value = 1), @IntParam(name = "intParam2", value = 12) }, str = { @StrParam(name = "strParam", value = "string value") }, dbl = { @DblParam(name = "dblParam", value = 0.65) }, date = { @DateParam(name = "dateParam", value = "2011-12-01 00:00:00") }, date_time = { @DateTimeParam(name = "dateTimeParam", value = "2011-12-01 00:00:00") }, money = { @MoneyParam(name = "moneyParam", value = "12.36") }, non_ordinary = { @ClassParam(name = "controllerParam", value = Controller.class) }, clazz = { @ClassParam(name = "classParam", value = String.class) })
    private String property1 = "default value";

    @IsProperty
    @MapTo
    @Title(value = "Property 2", desc = "Description")
    @AfterChange(value = InvalidAfterChangeEventHandler.class)
    private String property2;

    @Observable
    public Entity setProperty2(final String property2) {
        this.property2 = property2;
        return this;
    }

    public String getProperty2() {
        return property2;
    }

    @Observable
    public Entity setProperty1(final String property) {
        this.property1 = property;
        return this;
    }

    public String getProperty1() {
        return property1;
    }
}
