package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
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
public class TgExportFunctionalEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Parent Entity", desc = "Master entity for this functional entity")
    private TgPersistentEntityWithProperties parentEntity;

    @Observable
    @EntityExists(TgPersistentEntityWithProperties.class)
    public TgExportFunctionalEntity setParentEntity(final TgPersistentEntityWithProperties parentEntity) {
        this.parentEntity = parentEntity;
        return this;
    }

    public TgPersistentEntityWithProperties getParentEntity() {
        return parentEntity;
    }
}