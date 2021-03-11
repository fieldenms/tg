package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.User;

/**
 * A security token for entity {@link User} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class User_CanReadModel_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(User.class).getKey();
    public final static String TITLE = String.format(Template.READ_MODEL.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ_MODEL.forDesc(), ENTITY_TITLE);
}
