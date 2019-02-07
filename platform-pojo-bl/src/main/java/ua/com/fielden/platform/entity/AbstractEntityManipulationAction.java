package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

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
@KeyType(NoKey.class)
public abstract class AbstractEntityManipulationAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    @IsProperty
    @MapTo
    @Title("Entity Type")
    private String entityType;

    private Class<? extends AbstractEntity<?>> entityTypeAsClass;
    
    @IsProperty
    @MapTo
    @Title("Import URI")
    private String importUri;

    @IsProperty
    @MapTo
    @Title("Element Name")
    private String elementName;

    protected AbstractEntityManipulationAction() {
        setKey(NO_KEY);
    }
    
    @Observable
    public AbstractEntityManipulationAction setElementName(final String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    @Observable
    protected AbstractEntityManipulationAction setImportUri(final String importUri) {
        this.importUri = importUri;
        return this;
    }

    public String getImportUri() {
        return importUri;
    }

    public void setEntityTypeForEntityMaster(Class<? extends AbstractEntity<?>> entityTypeAsClass) {
        this.entityTypeAsClass = entityTypeAsClass;
        setEntityType(entityTypeAsClass.getName());
        setImportUri(format("/master_ui/%s", getEntityType()));
        setElementName(format("tg-%s-master", this.entityTypeAsClass.getSimpleName()));
    }

    public Class<? extends AbstractEntity<?>> getEntityTypeAsClass() {
        return entityTypeAsClass;
    }

    @Observable
    protected AbstractEntityManipulationAction setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }
}