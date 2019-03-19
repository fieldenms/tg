package ua.com.fielden.platform.security.tokens;

import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Top level security token for all security tokens that belong to main module. 
 *
 * @author TG Team
 *
 */
public class CompoundModuleToken implements ISecurityToken {
    public static final String TITLE = "Main";
    public static final String DESC = "A main module.";
}
