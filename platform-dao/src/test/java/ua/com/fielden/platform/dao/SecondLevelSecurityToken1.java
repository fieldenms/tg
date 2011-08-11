package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represents the second level security and added to the {@link FirstLevelSecurityToken1} group
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "SecondLevelSecurityToken1", desc = "SecondLevelSecurityToken1")
public class SecondLevelSecurityToken1 extends FirstLevelSecurityToken1 {
}
