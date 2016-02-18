package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
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
@CompanionObject(IEntityManipulationAction.class)
public class EntityManipulationAction extends AbstractFunctionalEntityWithCentreContext<String> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Entity Id", desc = "Entity id")
    private String entityId;

    @IsProperty
    @MapTo
    @Title(value = "Entity Type", desc = "Entity type")
    private String entityType;

    @IsProperty
    @MapTo
    @Title(value = "Import URI", desc = "Import uri")
    private String importUri;

    @IsProperty
    @MapTo
    @Title(value = "Element Name", desc = "Element name")
    private String elementName;

    @Observable
    public EntityManipulationAction setElementName(final String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    @Observable
    public EntityManipulationAction setImportUri(final String importUri) {
        this.importUri = importUri;
        return this;
    }

    public String getImportUri() {
        return importUri;
    }

    @Observable
    public EntityManipulationAction setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public EntityManipulationAction setEntityId(final String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

}