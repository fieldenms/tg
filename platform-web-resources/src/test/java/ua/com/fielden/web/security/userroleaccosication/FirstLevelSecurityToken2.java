package ua.com.fielden.web.security.userroleaccosication;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Another first level security token implemented for testing purpose only
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "FirstLevelSecurityToken2", desc = "FirstLevelSecurityToken2")
public class FirstLevelSecurityToken2 implements ISecurityToken {
}
