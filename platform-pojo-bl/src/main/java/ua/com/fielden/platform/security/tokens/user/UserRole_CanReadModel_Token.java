package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A security token for entity {@link UserRole} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class UserRole_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.READ_MODEL.forTitle(), UserRole.ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ_MODEL.forDesc(), UserRole.ENTITY_TITLE);
}