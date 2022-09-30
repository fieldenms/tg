package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A security token for entity {@link UserRole} to guard MASTER_OPEN.
 * 
 * @author TG Team
 */
public class UserRoleMaster_CanOpen_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), UserRole.ENTITY_TITLE + " Master");
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), UserRole.ENTITY_TITLE);
}