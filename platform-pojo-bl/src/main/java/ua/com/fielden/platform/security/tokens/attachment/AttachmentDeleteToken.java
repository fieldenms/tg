package ua.com.fielden.platform.security.tokens.attachment;

import static java.lang.String.format;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Attachment delete token.
 * 
 * @author TG Team
 * 
 */
@KeyTitle(value = "Attachment delete", desc = "Controls permission to delete attachments.")
public class AttachmentDeleteToken extends AttachmentModuleToken {
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(Attachment.class).getKey();
    public static final String TITLE = format(Template.DELETE.forTitle(), ENTITY_TITLE);
    public static final String DESC = format(Template.DELETE.forDesc(), ENTITY_TITLE);
}
