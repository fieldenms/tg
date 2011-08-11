package ua.com.fielden.platform.security.provider;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Represents a top level security token used for testing.
 * 
 * @author 01es
 * 
 */
@KeyTitle(value = "Top2LevelSecurityToken", desc = "Top level security token used for testing purposes.")
public class Top2LevelSecurityToken implements ISecurityToken {
}
