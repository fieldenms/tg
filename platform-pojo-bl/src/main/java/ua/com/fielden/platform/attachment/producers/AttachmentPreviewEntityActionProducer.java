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

public class AttachmentPreviewEntityActionProducer extends DefaultEntityProducerWithContext<AttachmentPreviewEntityAction> {

    @Inject
    public AttachmentPreviewEntityActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, AttachmentPreviewEntityAction.class, companionFinder);
    }

    @Override
    protected AttachmentPreviewEntityAction provideDefaultValues(final AttachmentPreviewEntityAction entity) {
        final Long attachmentId = getAttachmentId();
        if (attachmentId != null) {
            final IAttachment attachmentCo = co(Attachment.class);
            findByIdWithMasterFetch(attachmentCo, attachmentId)
                .ifPresent(attachment -> entity.setAttachmentUri(generateUri(attachment)));
        }
        return entity;
    }

    private String generateUri(final Attachment attachment) {
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
        } else if (chosenPropertyNotEmpty()) {
            if (Attachment.class.isAssignableFrom(currentEntity().get(chosenProperty()).getClass())) {
                return ((Attachment)currentEntity().get(chosenProperty())).getId();
            } else if (Attachment.class.isAssignableFrom(transform(currentEntity().getType(), chosenProperty()).getKey())) {
                return ((Attachment)currentEntity().get(penultAndLast(chosenProperty()).getKey())).getId();
            }
        }
        return null;
    }
}
