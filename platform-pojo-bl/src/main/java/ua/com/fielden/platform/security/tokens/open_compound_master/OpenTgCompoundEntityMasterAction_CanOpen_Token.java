package ua.com.fielden.platform.security.tokens.open_compound_master;

import ua.com.fielden.platform.sample.domain.compound.ui_actions.OpenTgCompoundEntityMasterAction;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link OpenTgCompoundEntityMasterAction} to guard Open.
 *
 * @author TG Team
 *
 */
public class OpenTgCompoundEntityMasterAction_CanOpen_Token extends CompoundModuleToken {
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), OpenTgCompoundEntityMasterAction.ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), OpenTgCompoundEntityMasterAction.ENTITY_TITLE);
}