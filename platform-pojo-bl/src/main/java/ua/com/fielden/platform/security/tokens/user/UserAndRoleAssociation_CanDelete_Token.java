package ua.com.fielden.platform.security.tokens.user;

import static java.lang.String.format;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

public class UserAndRoleAssociation_CanDelete_Token implements ISecurityToken {
    public final static String TITLE = format(Template.DELETE.forTitle(), UserAndRoleAssociation.ENTITY_TITLE);
    public final static String DESC = format(Template.DELETE.forDesc(), UserAndRoleAssociation.ENTITY_TITLE);
}