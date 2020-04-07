package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle("Reference Hierarchy")
@KeyType(NoKey.class)
@CompanionObject(IReferenceHierarchy.class)
public class ReferenceHierarchy extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Referenced Entity ID", desc = "Referenced Entity Id for which type level of hierarchy should be build")
    private Long refEntityId;

    @IsProperty
    @Title(value = "Referenced Entity Type", desc = "The type of Referenced Entity ID")
    private String refEntityType;

    @IsProperty
    @Title(value = "Entity Type", desc = "The type of entity that references the Referenced Entity ID'")
    private String entityType;

    @IsProperty
    @Title(value = "Reference Hierarchy Filter", desc = "Text to match entity types or entity instances")
    private String referenceHierarchyFilter;

    @Observable
    public ReferenceHierarchy setReferenceHierarchyFilter(final String referenceHierarchyFilter) {
        this.referenceHierarchyFilter = referenceHierarchyFilter;
        return this;
    }

    public String getReferenceHierarchyFilter() {
        return referenceHierarchyFilter;
    }

    @Observable
    public ReferenceHierarchy setRefEntityId(final Long refEntityId) {
        this.refEntityId = refEntityId;
        return this;
    }

    public Long getRefEntityId() {
        return refEntityId;
    }

    @Observable
    public ReferenceHierarchy setRefEntityType(final String refEntityType) {
        this.refEntityType = refEntityType;
        return this;
    }

    public String getRefEntityType() {
        return refEntityType;
    }

    @Observable
    public ReferenceHierarchy setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }
}