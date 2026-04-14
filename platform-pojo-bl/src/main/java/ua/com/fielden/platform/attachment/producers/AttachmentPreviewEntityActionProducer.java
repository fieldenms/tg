package ua.com.fielden.platform.attachment.producers;

import com.google.inject.Inject;
import ua.com.fielden.platform.attachment.*;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.SimpleMasterException;
import ua.com.fielden.platform.types.Hyperlink;

import java.util.function.Supplier;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.penultAndLast;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.transform;
import static ua.com.fielden.platform.utils.EntityUtils.findByIdWithMasterFetch;

/// Producer for [AttachmentPreviewEntityAction].
///
public class AttachmentPreviewEntityActionProducer extends DefaultEntityProducerWithContext<AttachmentPreviewEntityAction> {

    private static final String ERR_NOTHING_TO_VIEW = "There is nothing to view.";
    private static final Supplier<SimpleMasterException> NOTHING_TO_VIEW_EXCEPTION_SUPPLIER = () -> new SimpleMasterException(ERR_NOTHING_TO_VIEW);

    /// URI and kind carrier for [#generatePreview(Attachment)]; both fields are `null` when no preview is available.
    ///
    private record Preview(String uri, PreviewKind kind) {
        static final Preview NONE = new Preview(null, null);
    }

    @Inject
    public AttachmentPreviewEntityActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, AttachmentPreviewEntityAction.class, companionFinder);
    }

    @Override
    protected AttachmentPreviewEntityAction provideDefaultValues(final AttachmentPreviewEntityAction action) {
        final Long attachmentId = getAttachmentId();
        if (attachmentId == null) {
            throw NOTHING_TO_VIEW_EXCEPTION_SUPPLIER.get();
        }
        else {
            final IAttachment attachmentCo = co(Attachment.class);
            final Attachment attachment = findByIdWithMasterFetch(attachmentCo, attachmentId)
                    .orElseThrow(NOTHING_TO_VIEW_EXCEPTION_SUPPLIER);
            action.setAttachment(attachment);
            final Preview preview = generatePreview(attachment);
            action.setAttachmentUri(preview.uri());
            if (preview.kind() != null) {
                action.setKind(preview.kind());
            }
        }
        return action;
    }

    private Preview generatePreview(final Attachment attachment) {
        if (Hyperlink.validate(attachment.getTitle()).isSuccessful()) {
            return new Preview(attachment.getTitle(), PreviewKind.HYPERLINK);
        }
        final String mime = attachment.getMime();
        if (mime == null) {
            return Preview.NONE;
        }
        final String downloadUri = "/download-attachment/" + attachment.getId() + "/" + attachment.getSha1();
        // Images: served via <img>, which ignores Content-Disposition, so no inline flag is required.
        if (mime.startsWith("image/")) {
            return new Preview(downloadUri, PreviewKind.IMAGE);
        }
        // PDFs: rendered via <object>, which honours Content-Disposition; request inline serving.
        if ("application/pdf".equals(mime)) {
            return new Preview(downloadUri + "?inline=true", PreviewKind.PDF);
        }
        return Preview.NONE;
    }

    private Long getAttachmentId() {
        if (currentEntityInstanceOf(Attachment.class)) {
            return currentEntity().getId();
        } else if (currentEntityInstanceOf(AbstractAttachment.class)) {
            return ((AbstractAttachment<?,?>)currentEntity()).getAttachment().getId();
        } else if (chosenPropertyNotEmpty() && currentEntity().get(chosenProperty()) != null) {
            if (Attachment.class.isAssignableFrom(currentEntity().get(chosenProperty()).getClass())) {
                return ((Attachment)currentEntity().get(chosenProperty())).getId();
            } else if (Attachment.class.isAssignableFrom(transform(currentEntity().getType(), chosenProperty()).getKey())) {
                return ((Attachment)currentEntity().get(penultAndLast(chosenProperty()).getKey())).getId();
            }
        }
        return null;
    }
}
