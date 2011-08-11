package ua.com.fielden.platform.example.swing.securitytable;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represent the second level security token, added to the {@link FirstLevelSecurityToken2} group. Implemented for testing purpose only
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "Second Level Security Token 2", desc = "SecondLevelSecurityToken2")
public class SecondLevelSecurityToken2 extends FirstLevelSecurityToken2 {
}
