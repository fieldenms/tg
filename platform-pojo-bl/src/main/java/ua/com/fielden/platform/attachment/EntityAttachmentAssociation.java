package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * Class representing file attachment association with an entity of any type.
 *
 * Entity is represented by its ID, which relies on the act that ID values are unique across all entities.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("ATTACHMENT_ENTITY_ASSOCIATIONS")
public class EntityAttachmentAssociation extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @Title("Attachment")
    @CompositeKeyMember(1)
    @MapTo("ID_ATTACHMENTS")
    private Attachment attachment;

    @IsProperty
    @Title(value = "Domain Entity", desc = "An ID of the domain entity attachment is associated with.")
    @CompositeKeyMember(2)
    @MapTo("ID_ANY_ENTITY")
    private Long entityId;


    protected EntityAttachmentAssociation() {
	setKey(new DynamicEntityKey(this));
    }


    public Attachment getAttachment() {
        return attachment;
    }
    @Observable
    @EntityExists(Attachment.class)
    public void setAttachment(final Attachment attachment) {
        this.attachment = attachment;
    }


    public Long getEntityId() {
        return entityId;
    }
    @Observable
    @NotNull
    public void setEntityId(final Long entityId) {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return "association with " + getAttachment();
    }

}
