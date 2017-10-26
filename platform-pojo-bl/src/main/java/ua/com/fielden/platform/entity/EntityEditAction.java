package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An entity representing a generic <code>edit</code> action.
 *
 * @author TG Team
 *
 */
@CompanionObject(IEntityEditAction.class)
public class EntityEditAction extends AbstractEntityManipulationAction {

    @IsProperty
    @Title(value = "Entity Id", desc = "Entity id")
    private String entityId;

    @Observable
    public AbstractEntityManipulationAction setEntityId(final String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }
}