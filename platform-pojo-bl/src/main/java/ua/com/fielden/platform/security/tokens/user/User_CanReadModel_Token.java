package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.User;

/**
 * A security token for entity {@link User} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class User_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.READ_MODEL.forTitle(), User.ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ_MODEL.forDesc(), User.ENTITY_TITLE);
}