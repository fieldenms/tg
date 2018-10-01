package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An entity representing an attachment preview action.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(IAttachmentPreviewEntityAction.class)
public class AttachmentPreviewEntityAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title("Attachment URI")
    private String attachmentUri;

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
