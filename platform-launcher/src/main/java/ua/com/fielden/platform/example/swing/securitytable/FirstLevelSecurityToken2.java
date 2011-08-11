package ua.com.fielden.platform.example.swing.securitytable;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Another first level security token implemented for testing purpose only
 *
 * @author oleh
 *
 */
@KeyTitle(value = "First Level Security Token 2", desc = "FirstLevelSecurityToken2")
public class FirstLevelSecurityToken2 implements ISecurityToken {
}
