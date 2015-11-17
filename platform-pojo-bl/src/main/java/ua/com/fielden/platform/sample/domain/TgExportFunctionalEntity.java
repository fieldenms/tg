package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Export Functional Entity", desc = "Export Functional Entity description")
@CompanionObject(ITgExportFunctionalEntity.class)
public class TgExportFunctionalEntity extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Parent Entity", desc = "Master entity for this functional entity")
    private TgPersistentEntityWithProperties parentEntity;

    @IsProperty
    @Title(value = "Title", desc = "Desc")
    private Integer value;

    @IsProperty
    @Title(value = "Chosen property", desc = "A master entity property that is related to this action instance.")
    private String actionProperty;

    @Observable
    public TgExportFunctionalEntity setActionProperty(final String actionProperty) {
        this.actionProperty = actionProperty;
        return this;
    }

    public String getActionProperty() {
        return actionProperty;
    }

    

    
    @Observable
    public TgExportFunctionalEntity setValue(final Integer value) {
        this.value = value;
        return this;
    }

    public Integer getValue() {
        return value;
    }
    
    @Observable
    public TgExportFunctionalEntity setParentEntity(final TgPersistentEntityWithProperties parentEntity) {
        this.parentEntity = parentEntity;
        return this;
    }

    public TgPersistentEntityWithProperties getParentEntity() {
        return parentEntity;
    }
}