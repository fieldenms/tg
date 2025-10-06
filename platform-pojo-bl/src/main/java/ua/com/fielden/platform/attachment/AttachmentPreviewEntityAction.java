package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;

/// An entity representing an attachment preview action.
///
@KeyType(NoKey.class)
@CompanionObject(IAttachmentPreviewEntityAction.class)
public class AttachmentPreviewEntityAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title("Attachment")
    private Attachment attachment;

    @IsProperty
    @Title("Attachment URI")
    private String attachmentUri;

    public Attachment getAttachment() {
        return attachment;
    }

    @Observable
    public AttachmentPreviewEntityAction setAttachment(final Attachment attachment) {
        this.attachment = attachment;
        return this;
    }

    @Observable
    public AttachmentPreviewEntityAction setAttachmentUri(final String attachmentUri) {
        this.attachmentUri = attachmentUri;
        return this;
    }

    public String getAttachmentUri() {
        return attachmentUri;
    }

    protected AttachmentPreviewEntityAction() {
        setKey(NO_KEY);
    }

}