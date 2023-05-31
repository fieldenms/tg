package ua.com.fielden.platform.attachment.definers;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.attachment.Attachment.HYPERLINK;
import static ua.com.fielden.platform.attachment.Attachment.pn_TITLE;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Assigns attachment's property {@code title}, if it is empty, upon the first assignment of its property {@code originFileName}, which can only be assigned once.
 * 
 * @author TG Team
 *
 */
public class AssignAttachmentTitle implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String value) {
        final Attachment attachment = property.getEntity();
        final boolean isNotHyperlink = !HYPERLINK.equals(value);
        if (!attachment.isInitialising() && isNotHyperlink && isEmpty(attachment.getTitle())) {
            final String sanitisedValue = value.replace(",", " ")       // replace commas with a space
                                               .replaceAll("\\s+", " ") // replace sequential whitespace characters with a single space
                                               .trim();                 // finally, remove leading and trailing whitespace characters
            attachment.setTitle(sanitisedValue);
        }

        attachment.getProperty(pn_TITLE).setEditable(isNotHyperlink);
    }

}
