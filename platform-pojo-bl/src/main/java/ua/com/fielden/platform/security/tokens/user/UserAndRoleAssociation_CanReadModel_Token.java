package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/**
 * A security token for entity {@link UserAndRoleAssociation} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class UserAndRoleAssociation_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.READ_MODEL.forTitle(), UserAndRoleAssociation.ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ_MODEL.forDesc(), UserAndRoleAssociation.ENTITY_TITLE);
}