package ua.com.fielden.platform.example.swing.securitytable;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * First Level security token implemented only for testing purpose
 *
 * @author oleh
 *
 */
@KeyTitle(value = "First Level Security Token 1", desc = "Description for first level security token 1")
public class FirstLevelSecurityToken1 implements ISecurityToken {
}
