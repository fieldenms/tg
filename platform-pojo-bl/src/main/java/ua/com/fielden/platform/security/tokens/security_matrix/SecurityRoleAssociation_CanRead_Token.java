package ua.com.fielden.platform.security.tokens.security_matrix;

import ua.com.fielden.platform.keygen.KeyNumber;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

import static java.lang.String.format;

/// A security token for entity [SecurityRoleAssociation] to guard READ.
///
public class SecurityRoleAssociation_CanRead_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ.forTitle(), SecurityRoleAssociation.ENTITY_TITLE);
    public final static String DESC = format(Template.READ.forDesc(), UserAndRoleAssociation.ENTITY_TITLE);
}
