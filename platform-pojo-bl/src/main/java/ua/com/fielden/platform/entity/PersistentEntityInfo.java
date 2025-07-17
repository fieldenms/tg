package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.Pair;

import java.util.Date;

/// An action-entity that represents persistent version information about an entity that extends [AbstractPersistentEntity].
///
/// The actual data retrieval is implemented in [PersistentEntityInfoProducer].
///
/// #### Refactoring considerations
/// Note that the full name of this class is used in the JavaScript code.
/// For example, in `platform-web-ui/src/main/web/ua/com/fielden/platform/web/master/tg-entity-master-template-behavior.js`.
/// Extra care should be taken when renaming.
///
@EntityTitle("Persistent Entity Version Info")
@KeyType(DynamicEntityKey.class)
@CompanionObject(PersistentEntityInfoCo.class)
@KeyTitle("Persistent Entity ID")
public class PersistentEntityInfo extends AbstractFunctionalEntityWithCentreContext<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(PersistentEntityInfo.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @Title(value = "Unique Identifier", desc = "A unique identifier of this record at the database level.")
    @CompositeKeyMember(1)
    @Readonly
    private Long entityId;

    @IsProperty
    @Title(value = "Version", desc = "A version of this record. Indicates how many times it was changed. Value 0 corresponds to newly created records.")
    @Readonly
    @Required
    private Long entityVersion;

    @IsProperty
    @Title(value = "Created by User", desc = "A user who created this record.")
    @SkipEntityExistsValidation
    @Readonly
    private User createdBy;

    @IsProperty
    @Title(value = "Creation Date", desc = "A date/time when this record was created.")
    @Readonly
    private Date createdDate;

    @IsProperty
    @Title(value = "Last Updated By", desc = "A user who was the last to update this record.")
    @SkipEntityExistsValidation
    @Readonly
    private User lastUpdatedBy;

    @IsProperty
    @Title(value = "Last Updated Date", desc = "A date/time when this record was last updated.")
    @Readonly
    private Date lastUpdatedDate;

    @IsProperty
    @Title("Entity Title")
    private String entityTitle;

    @IsProperty
    @Title(value = "Entity Type", desc = "Entity Type to Inspect")
    private String entityType;

    @Observable
    public PersistentEntityInfo setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityTitle() {
        return entityTitle;
    }

    @Observable
    public PersistentEntityInfo setEntityTitle(final String entityTitle) {
        this.entityTitle = entityTitle;
        return this;
    }

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
    public PersistentEntityInfo setEntityId(final Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public Long getEntityId() {
        return entityId;
    }

}
