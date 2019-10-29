package ua.com.fielden.platform.security.tokens.attachment;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Download attachment token.
 * 
 * @author TG Team
 * 
 */
public class AttachmentDownload_CanExecute_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = "Attachment Download";
    public final static String TITLE = String.format(Template.EXECUTE.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.EXECUTE.forDesc(), ENTITY_TITLE);
}
