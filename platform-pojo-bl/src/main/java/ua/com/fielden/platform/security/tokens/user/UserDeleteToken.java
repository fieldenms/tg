package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

@KeyTitle(value = "User delete", desc = "Controls permission to delete users.")
public class UserDeleteToken extends UserReviewToken {

}
