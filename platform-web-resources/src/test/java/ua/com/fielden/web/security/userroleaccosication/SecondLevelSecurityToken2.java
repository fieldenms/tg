package ua.com.fielden.web.security.userroleaccosication;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represent the second level security token, added to the {@link FirstLevelSecurityToken2} group. Implemented for testing purpose only
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "SecondLevelSecurityToken2", desc = "SecondLevelSecurityToken2")
public class SecondLevelSecurityToken2 extends FirstLevelSecurityToken2 {
}
