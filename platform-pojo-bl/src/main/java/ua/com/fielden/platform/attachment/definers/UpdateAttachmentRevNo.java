package ua.com.fielden.platform.attachment.definers;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Updates attachment's property <code>revNo</code> based on its previous revision.
 * 
 * @author TG Team
 *
 */
public class UpdateAttachmentRevNo implements IAfterChangeEventHandler<Attachment> {

    @Override
    public void handle(final MetaProperty<Attachment> property, final Attachment prevRev) {
        final Attachment attachment = property.getEntity();
        if (!attachment.isInitialising()) {
            attachment.setRevNo(prevRev.getRevNo() + 1);
        }

    }

}
