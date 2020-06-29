package ua.com.fielden.platform.entity.before_change_event_handling;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.PropParam;

/**
 * Entity for the purpose of BCE handling tests.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithInvalidBceDefinition extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Description")
    @Required("Property cannot be null.")
    @BeforeChange({
            @Handler(
                    value = BeforeChangeEventHandler.class,
                    prop = { @PropParam(name = "propNameParam", propName = "property2") })})
    private String property1 = "default value";

    @Observable
    public EntityWithInvalidBceDefinition setProperty1(final String property) {
        this.property1 = property;
        return this;
    }

    public String getProperty1() {
        return property1;
    }
}
