package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.User;

public class User_CanDelete_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.DELETE.forTitle(), User.ENTITY_TITLE);
    public final static String DESC = String.format(Template.DELETE.forDesc(), User.ENTITY_TITLE);
}