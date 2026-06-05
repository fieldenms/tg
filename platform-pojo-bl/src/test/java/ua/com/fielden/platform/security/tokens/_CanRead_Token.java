package ua.com.fielden.platform.security.tokens;

import ua.com.fielden.platform.security.ISecurityToken;

/// A security token that controls whether reading should be permissive or not.
/// If access to this token is granted then reading is permitted for entity types, which were not provided with individual security tokens.
/// Effectively, this token controls the default authorisation behaviour for reading in the absence of specific authorisation tokens.
///
public class _CanRead_Token implements ISecurityToken {
    public final static String TITLE = "Can Read (if missing token)";
    public final static String DESC = "If access to this token is granted then reading would be permitted for entity types, which were not provided with individual security tokens.";
}
