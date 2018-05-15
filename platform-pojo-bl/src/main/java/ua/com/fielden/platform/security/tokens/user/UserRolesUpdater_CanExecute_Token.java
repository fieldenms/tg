package ua.com.fielden.platform.security.tokens.user;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserRolesUpdater;

public class UserRolesUpdater_CanExecute_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(UserRolesUpdater.class).getKey();
    public final static String TITLE = String.format(Template.EXECUTE.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.EXECUTE.forDesc(), ENTITY_TITLE);
}
