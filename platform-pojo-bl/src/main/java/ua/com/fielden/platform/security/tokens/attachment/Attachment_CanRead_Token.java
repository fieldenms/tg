package ua.com.fielden.platform.security.tokens.attachment;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link Attachment} to guard READ.
 * 
 * @author TG Team
 */
public class Attachment_CanRead_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(Attachment.class).getKey();
    public final static String TITLE = String.format(Template.READ.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ.forDesc(), ENTITY_TITLE);
}
