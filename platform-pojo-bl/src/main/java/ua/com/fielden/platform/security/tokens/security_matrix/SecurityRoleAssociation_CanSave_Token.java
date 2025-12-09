package ua.com.fielden.platform.security.tokens.security_matrix;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;

/// A security token for entity [SecurityRoleAssociation] to guard SAVE.
///
public class SecurityRoleAssociation_CanSave_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.SAVE.forTitle(), SecurityRoleAssociation.ENTITY_TITLE);
    public final static String DESC = String.format(Template.SAVE.forDesc(), SecurityRoleAssociation.ENTITY_TITLE);
}
