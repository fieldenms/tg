package ua.com.fielden.platform.entity;

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
public abstract class AbstractEntityManipulationAction extends AbstractFunctionalEntityWithCentreContext<String> {

    private static final long serialVersionUID = 1L;

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
    public AbstractEntityManipulationAction setElementName(final String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    @Observable
    public AbstractEntityManipulationAction setImportUri(final String importUri) {
        this.importUri = importUri;
        return this;
    }

    public String getImportUri() {
        return importUri;
    }

    @Observable
    public AbstractEntityManipulationAction setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }
}