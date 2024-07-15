package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.User;

public class User_CanSave_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.SAVE.forTitle(), User.ENTITY_TITLE);
    public final static String DESC = String.format(Template.SAVE.forDesc(), User.ENTITY_TITLE);
}