package ua.com.fielden.platform.security.tokens.security_matrix;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

import static java.lang.String.format;

/// A security token for entity [SecurityRoleAssociation] to guard READ_MODEL.
///
public class SecurityRoleAssociation_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ_MODEL.forTitle(), SecurityRoleAssociation.ENTITY_TITLE);
    public final static String DESC = format(Template.READ_MODEL.forDesc(), UserAndRoleAssociation.ENTITY_TITLE);
}
