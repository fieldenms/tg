package ua.com.fielden.platform.entity.before_change_event_handling;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.after_change_event_handling.AfterChangeEventHandler;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.PropParam;

/**
 * Entity for the purpose of ACE handling tests.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithInvalidAceDefinition extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Description")
    @Required("Property cannot be null.")
    @AfterChange(
            value = AfterChangeEventHandler.class,
            prop = { @PropParam(name = "propNameParam", propName = "property2") })
    private String property1 = "default value";

    @Observable
    public EntityWithInvalidAceDefinition setProperty1(final String property) {
        this.property1 = property;
        return this;
    }

    public String getProperty1() {
        return property1;
    }
}
