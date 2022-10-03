package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.User;

/**
 * A security token for entity {@link User} to guard MASTER_OPEN.
 * 
 * @author TG Team
 */
public class UserMaster_CanOpen_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = User.ENTITY_TITLE + " Master";
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), ENTITY_TITLE);
}