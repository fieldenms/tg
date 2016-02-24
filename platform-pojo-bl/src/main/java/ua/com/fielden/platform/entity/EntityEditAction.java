package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IEntityEditAction.class)
public class EntityEditAction extends AbstractEntityManipulationAction {
    private static final long serialVersionUID = 3129551522948219342L;

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