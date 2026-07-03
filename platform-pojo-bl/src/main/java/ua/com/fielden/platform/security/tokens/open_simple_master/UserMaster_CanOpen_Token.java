package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.security.tokens.open_compound_master.OpenUserMasterAction_CanOpen_Token;

/// A security token for entity [User] to guard MASTER_OPEN (opening a simple master).
///
/// @deprecated
/// The standard Web UI configuration for [User] provides a compound master, guarded by [OpenUserMasterAction_CanOpen_Token].
/// This token will be removed in the next major release, which will be a breaking change as it may be referenced
/// in existing applications (e.g., listed as excluded in custom [ISecurityTokenProvider] implementations).
/// This change should be accompanied by an SQL script to delete the corresponding [SecurityRoleAssociation] records.
///
@Deprecated(forRemoval = true)
public class UserMaster_CanOpen_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = User.ENTITY_TITLE + " Master";
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), ENTITY_TITLE);
}