package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/**
 * A security token for entity {@link UserAndRoleAssociation} to guard READ.
 * 
 * @author TG Team
 */
public class UserAndRoleAssociation_CanRead_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.READ.forTitle(), UserAndRoleAssociation.ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ.forDesc(), UserAndRoleAssociation.ENTITY_TITLE);
}