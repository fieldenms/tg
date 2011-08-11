package ua.com.fielden.platform.security;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represents a top level security token used for testing.
 * 
 * @author 01es
 * 
 */
@KeyTitle(value = "TopLevelSecurityToken", desc = "Top level security token used for testing purposes.")
public class TopLevelSecurityToken implements ISecurityToken {
}
