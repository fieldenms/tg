package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token that controls whether model reading should be permissive or not.
 * If access to this token is granted then model reading is permitted for entity types, which were not provided with individual security tokens.
 * Effectively, this token controls the default authorisation behaviour for model reading in the absence of specific authorisation tokens.
 * 
 * @author TG Team
 */
public class _CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = "Can Read Model (if missing token)";
    public final static String DESC = "If access to this token is granted then model reading would be permitted for entity types, which were not provided with individual security tokens.";
}