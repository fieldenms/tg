package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.Pair;

import java.util.Date;

/**
 * A functional entity that stores information about the persistent entity (including details about who created or updated it and when).
 *
 * @author TG Team
 */
@EntityTitle("Information about a persistent entity")
@KeyType(DynamicEntityKey.class)
@CompanionObject(PersistentEntityInfoCo.class)
@KeyTitle("Persistent Entity ID")
public class PersistentEntityInfo extends AbstractFunctionalEntityWithCentreContext<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(PersistentEntityInfo.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @Title(value = "Unique Identifier", desc = "Persistent Entity Unique Identifier")
    @CompositeKeyMember(1)
    @Readonly
    private Long entityId;

    @IsProperty
    @Title(value = "Entity Type", desc = "Persistent Entity Type")
    private String entityType;

    @IsProperty
    @Title(value = "Version", desc = "This entity version")
    @Readonly
    @Required
    private Long entityVersion;

    @IsProperty
    @Title(value = "Created by User", desc = "The user who originally created this entity instance.")
    @SkipEntityExistsValidation
    @Readonly
    private User createdBy;

    @IsProperty
    @Title(value = "Creation Date", desc = "The date/time when this entity instace was created.")
    @Readonly
    private Date createdDate;

    @IsProperty
    @Title(value = "Last Updated By", desc = "The user who was the last to update this entity instance.")
    @SkipEntityExistsValidation
    @Readonly
    private User lastUpdatedBy;

    @IsProperty
    @Title(value = "Last Updated Date", desc = "The date/time when this entity instance was last updated.")
    @Readonly
    private Date lastUpdatedDate;

    @Observable
    public PersistentEntityInfo setLastUpdatedDate(final Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    @Observable
    public PersistentEntityInfo setLastUpdatedBy(final User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Observable
    public PersistentEntityInfo setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Observable
    public PersistentEntityInfo setCreatedBy(final User createdByUser) {
        this.createdBy = createdByUser;
        return this;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    @Observable
    public PersistentEntityInfo setEntityVersion(final Long entityVersion) {
        this.entityVersion = entityVersion;
        return this;
    }

    public Long getEntityVersion() {
        return entityVersion;
    }

    @Observable
    public PersistentEntityInfo setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public PersistentEntityInfo setEntityId(final Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public Long getEntityId() {
        return entityId;
    }

}
