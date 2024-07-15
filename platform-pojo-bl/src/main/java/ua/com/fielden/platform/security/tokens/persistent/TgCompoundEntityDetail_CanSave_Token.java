package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link TgCompoundEntityDetail} to guard Save.
 *
 * @author TG Team
 *
 */
public class TgCompoundEntityDetail_CanSave_Token extends CompoundModuleToken {
    public final static String TITLE = String.format(Template.SAVE.forTitle(), TgCompoundEntityDetail.ENTITY_TITLE);
    public final static String DESC = String.format(Template.SAVE.forDesc(), TgCompoundEntityDetail.ENTITY_TITLE);
}