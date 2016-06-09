package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * User role review security token.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "User role review", desc = "Controls permission to select and review user roles.")
public class UserRoleReviewToken implements ISecurityToken {
}
