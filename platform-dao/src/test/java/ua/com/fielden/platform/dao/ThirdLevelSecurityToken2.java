package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Third level security token added to the {@link SecondLevelSecurityToken2} group. Implemented only for testing purpose
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "ThirdLevelSecurityToken2", desc = "ThirdLevelSecurityToken2")
public class ThirdLevelSecurityToken2 extends SecondLevelSecurityToken2 {

}
