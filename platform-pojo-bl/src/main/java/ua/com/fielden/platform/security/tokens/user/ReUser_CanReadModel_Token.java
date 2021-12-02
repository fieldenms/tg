package ua.com.fielden.platform.security.tokens.user;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.ReUser;

/**
 * A security token for entity {@link ReUser} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class ReUser_CanReadModel_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(ReUser.class).getKey();
    public final static String TITLE = format(Template.READ_MODEL.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(Template.READ_MODEL.forDesc(), ENTITY_TITLE);
}