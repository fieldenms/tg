package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.User;

/**
 * A security token for entity {@link User} to guard READ.
 * 
 * @author TG Team
 */
public class User_CanRead_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.READ.forTitle(), User.ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ.forDesc(), User.ENTITY_TITLE);
}