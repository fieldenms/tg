package ua.com.fielden.platform.attachment.validators;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

public class CanBeUsedAsLastAttachmentRev extends AbstractBeforeChangeEventHandler<Attachment> {
    public static final String ERR_SELF_REFERENCE = "Cannot reference itself as [%s] when there is no revision history.";
    public static final String ERR_LAST_REV_CANNOT_BE_EMPTY = "Property [%s] cannot be emptied when there is a revision history.";
    public static final String ERR_REBASING_REVISION = format("%s is already used as part of a revision history. Rebasing revisions is not permitted.", Attachment.ENTITY_TITLE);

    @Override
    public Result handle(final MetaProperty<Attachment> property, final Attachment lastRevision, final Set<Annotation> mutatorAnnotations) {
        final Attachment attachment = property.getEntity();
        
        if (equalsEx(attachment, lastRevision) && attachment.getRevNo() == 0) {
            return failure(format(ERR_SELF_REFERENCE, property.getTitle()));
        }
        
        if (lastRevision == null && (attachment.getRevNo() > 0 || attachment.getLastRevision() != null)) {
            return failure(format(ERR_LAST_REV_CANNOT_BE_EMPTY, property.getTitle()));
        }

        if (lastRevision != null && attachment.getLastRevision() != null && !attachment.isLastRevisionUpdateAllowed()) {
            return failure(ERR_REBASING_REVISION);
        }

        return successful(lastRevision);
    }

}
