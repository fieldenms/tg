package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An abstract entity that is used as a common super type for generic entities {@link EntityEditAction} and {@link EntityNewAction}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public abstract class AbstractEntityManipulationAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    @IsProperty
    @MapTo
    @Title("Entity Type")
    private String entityType;

    @IsProperty
    @MapTo
    @Title("Import URI")
    private String importUri;

    @IsProperty
    @MapTo
    @Title("Element Name")
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