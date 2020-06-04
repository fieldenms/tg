package ua.com.fielden.platform.ref_hierarchy;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the type entry in reference hierarchy.
 *
 * @author TG Team
 *
 */
@KeyTitle("Entity Type Title")
@DescTitle("Entity Type Description")
public class TypeLevelHierarchyEntry extends ReferenceHierarchyEntry {

    @IsProperty
    @Title(value = "Entity Type", desc = "The Referencing entity type")
    private String entityType;

    @IsProperty
    @Title("Refernced Entity ID")
    private Long refEntityId;

    @IsProperty
    @Title("Referenced Entity Type")
    private String refEntityType;

    @IsProperty
    @Title(value = "Number of Entities", desc = "The number of entities of this type those refrences the entity of upper level entity")
    private Integer numberOfEntities;

    @IsProperty
    @Title(value = "Page Size", desc = "Page size of inctances to load")
    private Integer pageSize = 10;

    @IsProperty
    @Title(value = "Page Number", desc = "Page number of instances to load")
    private Integer pageNumber = 0;

    @Observable
    public TypeLevelHierarchyEntry setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public TypeLevelHierarchyEntry setRefEntityType(final String refEntityType) {
        this.refEntityType = refEntityType;
        return this;
    }

    public String getRefEntityType() {
        return refEntityType;
    }

    @Observable
    public TypeLevelHierarchyEntry setRefEntityId(final Long refEntityId) {
        this.refEntityId = refEntityId;
        return this;
    }

    public Long getRefEntityId() {
        return refEntityId;
    }

    @Observable
    public TypeLevelHierarchyEntry setPageNumber(final Integer pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    @Observable
    public TypeLevelHierarchyEntry setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    @Observable
    public TypeLevelHierarchyEntry setNumberOfEntities(final Integer numberOfEntities) {
        this.numberOfEntities = numberOfEntities;
        return this;
    }

    public Integer getNumberOfEntities() {
        return numberOfEntities;
    }
}
