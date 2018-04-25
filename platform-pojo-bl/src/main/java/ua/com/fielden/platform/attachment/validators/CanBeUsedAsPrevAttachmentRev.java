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

public class CanBeUsedAsPrevAttachmentRev extends AbstractBeforeChangeEventHandler<Attachment> {
    public static final String ERR_SELF_REFERENCE = "Cannot reference itself as [%s].";
    public static final String ERR_REBASING_REVISION = format("%s is already used as previous revision. Rebasing revisions is not permitted.", Attachment.ENTITY_TITLE);
    public static final String ERR_DUPLICATE_SHA1 = format("%s revision history does not permit duplicate documents.", Attachment.ENTITY_TITLE) + "[SHA1: %s].";

    @Override
    public Result handle(final MetaProperty<Attachment> property, final Attachment prevRevision, final Set<Annotation> mutatorAnnotations) {
        final Attachment attachment = property.getEntity();
        
        if (equalsEx(attachment, prevRevision)) {
            return failure(format(ERR_SELF_REFERENCE, property.getTitle()));
        }
        
        if (prevRevision.getLastRevision() != null && !equalsEx(prevRevision, prevRevision.getLastRevision()) && !equalsEx(attachment, prevRevision)) {
            return failure(ERR_REBASING_REVISION);
        }
        
        if (equalsEx(attachment.getSha1(), prevRevision.getSha1())) {
            return failure(format(ERR_DUPLICATE_SHA1, prevRevision.getSha1()));
        }
        
        return successful(prevRevision);
    }

}
