package ua.com.fielden.platform.security.tokens.user;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

public class UserAndRoleAssociation_CanDelete_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(UserAndRoleAssociation.class).getKey();
    public final static String TITLE = format(Template.DELETE.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(Template.DELETE.forDesc(), ENTITY_TITLE);
}