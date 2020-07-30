package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.error.Result;

/**
 * Entity that has its {@code isEditable} overridden to be dependent on the value of property {@code notEditableIfFalse}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@MapEntityTo
public class EntityWithEditabilityDependenOnState extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Not Editable If False", desc = "Makes entity read-only if the value is false.")
    private boolean notEditableIfFalse = true;

    @Observable
    public EntityWithEditabilityDependenOnState setNotEditableIfFalse(final boolean notEditableIfFalse) {
        this.notEditableIfFalse = notEditableIfFalse;
        return this;
    }

    public boolean getNotEditableIfFalse() {
        return notEditableIfFalse;
    }

    @Override
    public Result isEditable() {
        final Result defaultRes = super.isEditable();
        if (!defaultRes.isSuccessful()) {
            return defaultRes; 
        }
        return getNotEditableIfFalse() ? successful(this) : failure("Not editable");
    }

}