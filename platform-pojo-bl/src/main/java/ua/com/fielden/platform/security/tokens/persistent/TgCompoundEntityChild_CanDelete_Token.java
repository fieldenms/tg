package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link TgCompoundEntityChild} to guard Delete.
 *
 * @author TG Team
 *
 */
public class TgCompoundEntityChild_CanDelete_Token extends CompoundModuleToken {
    public final static String TITLE = String.format(Template.DELETE.forTitle(), TgCompoundEntityChild.ENTITY_TITLE);
    public final static String DESC = String.format(Template.DELETE.forDesc(), TgCompoundEntityChild.ENTITY_TITLE);
}