package ua.com.fielden.platform.security.tokens.user;

import static java.lang.String.format;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.ReUser;

/**
 * A security token for entity {@link ReUser} to guard READ.
 * 
 * @author TG Team
 */
public class ReUser_CanRead_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ.forTitle(), ReUser.ENTITY_TITLE);
    public final static String DESC = format(Template.READ.forDesc(), ReUser.ENTITY_TITLE);
}