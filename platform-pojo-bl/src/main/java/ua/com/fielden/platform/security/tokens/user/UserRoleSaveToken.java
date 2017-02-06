package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

@KeyTitle(value = "User role save", desc = "Controls permission to save new or changes to existing user roles.")
public class UserRoleSaveToken extends UserRoleReviewToken {

}
