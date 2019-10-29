package ua.com.fielden.platform.security.tokens.attachment;

import static java.lang.String.format;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Attachment delete token.
 * 
 * @author TG Team
 * 
 */
public class Attachment_CanDelete_Token implements ISecurityToken {
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(Attachment.class).getKey();
    public static final String TITLE = format(Template.DELETE.forTitle(), ENTITY_TITLE);
    public static final String DESC = format(Template.DELETE.forDesc(), ENTITY_TITLE);
}
