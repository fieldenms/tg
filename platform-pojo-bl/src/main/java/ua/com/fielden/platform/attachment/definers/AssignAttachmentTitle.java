package ua.com.fielden.platform.attachment.definers;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Assigns attachment's property <code>title</code> if it is empty, upon the first assignment of its property <code>originFileName</code>, which can only be assigned once.
 * 
 * @author TG Team
 *
 */
public class AssignAttachmentTitle implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String value) {
        final Attachment attachment = property.getEntity();
        if (!attachment.isInitialising() && StringUtils.isEmpty(attachment.getTitle())) {
            attachment.setTitle(value);
        }

    }

}
