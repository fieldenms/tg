package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.EntityMasterHelp;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link EntityMasterHelp} to guard Save.
 *
 * @author TG Team
 *
 */
public class EntityMasterHelp_CanSave_Token implements ISecurityToken {
    public final static String TITLE = format(Template.SAVE.forTitle(), EntityMasterHelp.ENTITY_TITLE);
    public final static String DESC = format(Template.SAVE.forDesc(), EntityMasterHelp.ENTITY_TITLE);
}