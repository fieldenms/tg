package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

@KeyTitle(value = "User save", desc = "Controls permission to save new or changes to existing users.")
public class UserSaveToken extends UserReviewToken {

}
