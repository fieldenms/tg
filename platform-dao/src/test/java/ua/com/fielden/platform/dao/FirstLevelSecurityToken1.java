package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * First Level security token implemented only for testing purpose
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "FirstLevelSecurityToken1", desc = "FirstLevelSecurityToken1")
public class FirstLevelSecurityToken1 implements ISecurityToken {
}
