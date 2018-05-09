package ua.com.fielden.platform.security.tokens.attachment;

import static java.lang.String.format;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Attachment save/upload token.
 * 
 * @author TG Team
 * 
 */
public class Attachment_CanSave_Token implements ISecurityToken {
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(Attachment.class).getKey();
    public static final String TITLE = format(Template.SAVE.forTitle(), ENTITY_TITLE);
    public static final String DESC = format(Template.SAVE.forDesc(), ENTITY_TITLE);
}
