package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * User review security token.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "User review", desc = "Controls permission to select and review user data.")
public class UserReviewToken implements ISecurityToken {
}
