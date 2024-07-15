package ua.com.fielden.security.tokens.open_compound_master;

import static java.lang.String.format;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.security.user.ui_actions.OpenUserMasterAction;

/**
 * A security token for entity {@link OpenUserMasterAction} to guard Open.
 *
 * @author TG Team
 *
 */
public class OpenUserMasterAction_CanOpen_Token implements ISecurityToken {
    public final static String TITLE = format(Template.MASTER_OPEN.forTitle(), OpenUserMasterAction.ENTITY_TITLE);
    public final static String DESC = format(Template.MASTER_OPEN.forDesc(), OpenUserMasterAction.ENTITY_TITLE);
}