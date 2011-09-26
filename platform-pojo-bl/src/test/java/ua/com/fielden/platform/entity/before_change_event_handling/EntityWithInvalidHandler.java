package ua.com.fielden.platform.entity.before_change_event_handling;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.NotNullValidator;

/**
 * Entity for the purpose of BCE handling tests.
 * It has a setter annotated with invalid BCE handler, thus causing exceptional situation during instantiation.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithInvalidHandler extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Description")
    private String property1;

    @Observable
    @BeforeChange({
	@Handler(value = InvalidBeforeChangeEventHandler.class,
		 non_ordinary={@ClassParam(name = "invalidParam", value = BeforeChangeEventHandler.class)}),
        @Handler(NotNullValidator.class)
    })
    public EntityWithInvalidHandler setProperty1(final String property) {
	this.property1 = property;
	return this;
    }

    public String getProperty1() {
	return property1;
    }
}
