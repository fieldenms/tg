package ua.com.fielden.platform.attachment.validators;

import static java.lang.String.format;
import static ua.com.fielden.platform.attachment.Attachment.pn_PREV_REVISION;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Ensures that the specified revision number, which should only be determined algorithmically, is aligned with the previous revision of the attachment.
 * <p>
 * If the previous revision does not exist then the only permitted value is zero.
 * Otherwise, the value should be greater than the revision number of the previous revision by 1.
 * 
 * @author TG Team
 *
 */
public class IsRevNoAlignedWithPrevRevision implements IBeforeChangeEventHandler<Integer> {
    public static final String ERR_INVALID_REV_NO = format("Expected revision number for [%s]", Attachment.ENTITY_TITLE) + " is [%s] (attempted value is [%s]).";
    
    
    @Override
    public Result handle(final MetaProperty<Integer> property, final Integer newRevNo, final Set<Annotation> mutatorAnnotations) {
        final Attachment attachment = property.getEntity();
        
        if (attachment.getPrevRevision() == null) {
            if (newRevNo == 0) {
                return successful(newRevNo);
            } else {
                return failure(format(ERR_INVALID_REV_NO, 0, newRevNo));
            }
        } else if (attachment.getProperty(pn_PREV_REVISION).isDirty()) {
            final int diff = newRevNo - attachment.getPrevRevision().getRevNo();
            if (diff != 1) {
                return failure(format(ERR_INVALID_REV_NO, attachment.getPrevRevision().getRevNo() + 1, newRevNo));
            } else {
                return successful(newRevNo);
            }
        }
        return successful(newRevNo);
    }

}
