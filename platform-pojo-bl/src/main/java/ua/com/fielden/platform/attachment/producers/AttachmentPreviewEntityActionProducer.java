package ua.com.fielden.platform.attachment.producers;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.penultAndLast;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.transform;
import static ua.com.fielden.platform.utils.EntityUtils.findByIdWithMasterFetch;

import com.google.inject.Inject;

import ua.com.fielden.platform.attachment.AbstractAttachment;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.SimpleMasterException;
import ua.com.fielden.platform.types.Hyperlink;

import java.util.function.Supplier;

/**
 * Producer for {@link AttachmentPreviewEntityAction}.
 *
 * @author TG Team
 *
 */
public class AttachmentPreviewEntityActionProducer extends DefaultEntityProducerWithContext<AttachmentPreviewEntityAction> {

    private static final String NOTHING_TO_VIEW_MSG = "There is nothing to preview.";
    private static final Supplier<? extends RuntimeException> NOTHING_TO_VIEW_EXCEPTION_SUPPLIER = () -> new SimpleMasterException(NOTHING_TO_VIEW_MSG);

    @Inject
    public AttachmentPreviewEntityActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, AttachmentPreviewEntityAction.class, companionFinder);
    }

    @Override
    protected AttachmentPreviewEntityAction provideDefaultValues(final AttachmentPreviewEntityAction entity) {
        final Long attachmentId = getAttachmentId();
        if (attachmentId != null) {
            final IAttachment attachmentCo = co(Attachment.class);
            final Attachment attachment = findByIdWithMasterFetch(attachmentCo, attachmentId)
                    .orElseThrow(NOTHING_TO_VIEW_EXCEPTION_SUPPLIER);
            entity.setAttachment(attachment);
            entity.setAttachmentUri(generateUri(attachment));
        } else {
            throw new SimpleMasterException(NOTHING_TO_VIEW_MSG);
        }
        return entity;
    }

    private String generateUri(final Attachment attachment) {
        if(Hyperlink.validate(attachment.getTitle()).isSuccessful()) {
            return attachment.getTitle();
        }
        if (attachment.getMime() != null && attachment.getMime().contains("image")) {
            return "/download-attachment/" + attachment.getId() + "/" + attachment.getSha1();
        }
        return null;
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
