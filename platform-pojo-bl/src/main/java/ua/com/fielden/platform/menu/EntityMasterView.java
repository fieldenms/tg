package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IEntityMasterView.class)
public class EntityMasterView extends AbstractView {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Entity Id", desc = "Entity id")
    private String entityId;

    @IsProperty
    @Title(value = "Centre UUID", desc = "Centre uniq identifier")
    private String centreUuid;

    @IsProperty
    @Title(value = "Current State", desc = "Current master state")
    private String currentState;

    @IsProperty
    @Title(value = "Entity Type", desc = "Entity type")
    private String entityType;

    @IsProperty
    @Title(value = "UUID", desc = "Unique identifier")
    private String uuid;

    @Observable
    public EntityMasterView setUuid(final String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    @Observable
    public EntityMasterView setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public EntityMasterView setCurrentState(final String currentState) {
        this.currentState = currentState;
        return this;
    }

    public String getCurrentState() {
        return currentState;
    }

    @Observable
    public EntityMasterView setCentreUuid(final String centreUuid) {
        this.centreUuid = centreUuid;
        return this;
    }

    public String getCentreUuid() {
        return centreUuid;
    }

    @Observable
    public EntityMasterView setEntityId(final String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

}