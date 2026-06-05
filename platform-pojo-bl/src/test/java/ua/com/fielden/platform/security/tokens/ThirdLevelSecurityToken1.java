package ua.com.fielden.platform.security.tokens;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.tokens.SecondLevelSecurityToken1;

/**
 * Represent the lowest level of security tokens. Implemented only for the testing purpose.
 * 
 * @author oleh
 * 
 */
@KeyTitle(value = "ThirdLevelSecurityToken1", desc = "ThirdLevelSecurityToken1")
public class ThirdLevelSecurityToken1 extends SecondLevelSecurityToken1 {

}
