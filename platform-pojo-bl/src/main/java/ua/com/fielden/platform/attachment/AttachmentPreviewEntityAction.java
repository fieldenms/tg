package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Optional;

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

    @IsProperty
    @Title("Kind")
    private String kind;

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

    @Observable
    protected AttachmentPreviewEntityAction setKind(final String kind) {
        this.kind = kind;
        return this;
    }

    private String getKind() {
        return kind;
    }

    public AttachmentPreviewEntityAction setKind(final PreviewKind kind) {
        return this.setKind(kind.name());
    }

    /// Returns the [preview kind][PreviewKind] for this attachment, or empty if no preview is available.
    ///
    public Optional<PreviewKind> kind() {
        return PreviewKind.of(kind);
    }

    protected AttachmentPreviewEntityAction() {
        setKey(NO_KEY);
    }

}