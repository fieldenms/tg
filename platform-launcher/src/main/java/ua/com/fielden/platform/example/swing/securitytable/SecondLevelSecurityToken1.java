package ua.com.fielden.platform.example.swing.securitytable;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represents the second level security and added to the {@link FirstLevelSecurityToken1} group
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "Second Level Security Token 1", desc = "SecondLevelSecurityToken1")
public class SecondLevelSecurityToken1 extends FirstLevelSecurityToken1 {
}
