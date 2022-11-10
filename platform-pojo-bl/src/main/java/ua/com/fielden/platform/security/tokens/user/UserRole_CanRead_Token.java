package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A security token for entity {@link UserRole} to guard READ.
 * 
 * @author TG Team
 */
public class UserRole_CanRead_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.READ.forTitle(), UserRole.ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ.forDesc(), UserRole.ENTITY_TITLE);
}