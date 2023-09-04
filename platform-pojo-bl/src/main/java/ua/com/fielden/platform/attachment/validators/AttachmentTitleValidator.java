package ua.com.fielden.platform.attachment.validators;

import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Hyperlink;

/**
 * Ensures that {@code Attachment.title} restricts extra spaces and commas for titles in case of file attachments, and also that title values represent valid URL in case of
 * hyperlink attachments.
 * 
 * @author TG Team
 *
 */
public class AttachmentTitleValidator implements IBeforeChangeEventHandler<String> {

    @Override
    public Result handle(final MetaProperty<String> property, final String newTitle, final Set<Annotation> mutatorAnnotations) {
        final Attachment attachment = property.getEntity();
        if (attachment.isHyperlinkAttachment()) {
            return Hyperlink.validate(newTitle);
        }
        // otherwise, this is a file attachment and we need to validate title differently
        final Result spacesRes = new RestrictExtraWhitespaceValidator().handle(property, newTitle, mutatorAnnotations);
        if (!spacesRes.isSuccessful()) {
            return spacesRes;
        }
        final Result commasRes = new RestrictCommasValidator().handle(property, newTitle, mutatorAnnotations);
        if (!commasRes.isSuccessful()) {
            return commasRes;
        }
        return successful();
    }

}
