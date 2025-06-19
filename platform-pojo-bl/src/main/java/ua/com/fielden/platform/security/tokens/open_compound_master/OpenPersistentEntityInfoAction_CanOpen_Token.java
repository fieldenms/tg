package ua.com.fielden.platform.security.tokens.open_compound_master;

import ua.com.fielden.platform.entity.OpenPersistentEntityInfoAction;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/// A security token for entity {@link OpenPersistentEntityInfoAction} to guard MASTER_OPEN.
///
public class OpenPersistentEntityInfoAction_CanOpen_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(OpenPersistentEntityInfoAction.class).getKey();
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), ENTITY_TITLE);
}
