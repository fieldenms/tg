package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;

/// The entity that represents the row in domain explorer tree.
///
@KeyType(String.class)
@KeyTitle(value = "Entity Type Title", desc = "The title of entity type")
@DescTitle(value="Entity Type Description", desc="The description of entity type")
public class DomainTreeEntity extends AbstractTreeEntry<String> {

    @IsProperty
    @Title(value = "Property Type", desc = "Property Type Title")
    private DomainType propertyType;

    @IsProperty
    @Title(value = "Internal Name", desc = "Internal type/property name")
    private String internalName;

    @IsProperty
    @Title(value = "DB Schema", desc = "The data base table/property name")
    private String dbSchema;

    @IsProperty
    @Title(value = "Ref. Table", desc = "Referencing Table")
    private String refTable;

    @IsProperty
    @Title("Tree Entity Id")
    private Long entityId;

    @Observable
    public DomainTreeEntity setEntityId(final Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public Long getEntityId() {
        return entityId;
    }

    @Observable
    public DomainTreeEntity setRefTable(final String refTable) {
        this.refTable = refTable;
        return this;
    }

    public String getRefTable() {
        return refTable;
    }

    @Observable
    public DomainTreeEntity setDbSchema(final String dbSchema) {
        this.dbSchema = dbSchema;
        return this;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    @Observable
    public DomainTreeEntity setInternalName(final String internalName) {
        this.internalName = internalName;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    @Observable
    public DomainTreeEntity setPropertyType(final DomainType propertyType) {
        this.propertyType = propertyType;
        return this;
    }

    public DomainType getPropertyType() {
        return propertyType;
    }

    @Override
    @Observable
    public  DomainTreeEntity setDesc(String desc) {
        return super.setDesc(desc);
    }

}
