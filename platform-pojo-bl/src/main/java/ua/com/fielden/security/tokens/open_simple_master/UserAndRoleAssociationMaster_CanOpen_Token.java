package ua.com.fielden.security.tokens.open_simple_master;

import static java.lang.String.format;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/**
 * A security token for entity {@link UserAndRoleAssociation} to guard Open.
 *
 * @author TG Team
 *
 */
public class UserAndRoleAssociationMaster_CanOpen_Token implements ISecurityToken {
    public final static String TITLE = format(Template.MASTER_OPEN.forTitle(), UserAndRoleAssociation.ENTITY_TITLE + " Master");
    public final static String DESC = format(Template.MASTER_OPEN.forDesc(), UserAndRoleAssociation.ENTITY_TITLE);
}