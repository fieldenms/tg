package ua.com.fielden.platform.security;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represents a lower level security token used for testing.
 * 
 * @author 01es
 * 
 */
@KeyTitle(value = "LowerLevelSecurityToken", desc = "Lower level security token used for testing purposes.")
public class LowerLevelSecurityToken extends TopLevelSecurityToken {
}
