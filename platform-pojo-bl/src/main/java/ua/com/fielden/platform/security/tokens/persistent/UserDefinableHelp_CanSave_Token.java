package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.UserDefinableHelp;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link UserDefinableHelp} to guard Save.
 *
 * @author TG Team
 *
 */
public class UserDefinableHelp_CanSave_Token implements ISecurityToken {
    public final static String TITLE = format(Template.SAVE.forTitle(), UserDefinableHelp.ENTITY_TITLE);
    public final static String DESC = format(Template.SAVE.forDesc(), UserDefinableHelp.ENTITY_TITLE);
}