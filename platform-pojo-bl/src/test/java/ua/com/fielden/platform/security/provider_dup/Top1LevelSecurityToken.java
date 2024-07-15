package ua.com.fielden.platform.security.provider_dup;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Represents a top level security token used for testing, which duplicates a simple name of a security token {@link ua.com.fielden.platform.security.provider.Top1LevelSecurityToken}.
 * 
 * @author TG Team
 * 
 * 
 */
@KeyTitle(value = "Top1LevelSecurityToken", desc = "Top level security token used for testing purposes.")
public class Top1LevelSecurityToken implements ISecurityToken {

}