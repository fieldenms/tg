package ua.com.fielden.platform.example.swing.securitytable;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Third level security token added to the {@link SecondLevelSecurityToken2} group. Implemented only for testing purpose
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "Third Level Security Token 2", desc = "ThirdLevelSecurityToken2")
public class ThirdLevelSecurityToken2 extends SecondLevelSecurityToken2 {

}
