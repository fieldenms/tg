package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An entity representing an attachment for {@link TgPersistentEntityWithProperties}.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@CompanionObject(ITgPersistentEntityWithPropertiesAttachment.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Custom attachment description, potentially explaining its need and role.")
public class TgPersistentEntityWithPropertiesAttachment extends AbstractPersistentEntity<DynamicEntityKey> {
    
    @IsProperty
    @MapTo
    @Title(value = "Master", desc = "Master entity with which attachment is associated.")
    @CompositeKeyMember(1)
    private TgPersistentEntityWithProperties master;

    @IsProperty
    @MapTo
    @Title(value = "Attachment", desc = "Desc")
    @CompositeKeyMember(2)
    private Attachment attachment;

    @Observable
    public TgPersistentEntityWithPropertiesAttachment setAttachment(final Attachment attachment) {
        this.attachment = attachment;
        return this;
    }

    public Attachment getAttachment() {
        return attachment;
    }
    
    @Observable
    public TgPersistentEntityWithPropertiesAttachment setMaster(final TgPersistentEntityWithProperties master) {
        this.master = master;
        return this;
    }

    public TgPersistentEntityWithProperties getMaster() {
        return master;
    }

    

}