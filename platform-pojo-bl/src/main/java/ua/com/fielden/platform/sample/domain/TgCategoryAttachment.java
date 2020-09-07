package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.attachment.AbstractAttachment;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

/**
 * Master entity object to represent an association between {@link TgCategory} and {@link Attachment}.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Category Attachment")
@EntityTitle("Category Attachment")
@CompanionObject(ITgCategoryAttachment.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Should be used to describe the purpose of this attachment for the associated work activity.")
public class TgCategoryAttachment extends AbstractAttachment<TgCategoryAttachment, TgCategory> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private TgCategory attachedTo;

    
    @Override
    @Observable
    public TgCategoryAttachment setAttachedTo(final TgCategory value) {
        this.attachedTo = value;
        return this;
    }

    @Override
    public TgCategory getAttachedTo() {
        return attachedTo;
    }

    @Override
    @Observable
    public TgCategoryAttachment setAttachment(final Attachment attachment) {
        super.setAttachment(attachment);
        return this;
    }
}