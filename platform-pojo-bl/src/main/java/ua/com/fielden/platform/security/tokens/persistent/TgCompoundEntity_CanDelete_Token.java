package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link TgCompoundEntity} to guard Delete.
 *
 * @author Developers
 *
 */
public class TgCompoundEntity_CanDelete_Token extends CompoundModuleToken {
    public final static String TITLE = String.format(Template.DELETE.forTitle(), TgCompoundEntity.ENTITY_TITLE);
    public final static String DESC = String.format(Template.DELETE.forDesc(), TgCompoundEntity.ENTITY_TITLE);
}