package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/// A security token for entity [TgBogie] to guard master open.
///
public class TgBogieMaster_CanOpen_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(TgBogie.class).getKey() + " Master";
    public final static String TITLE = Template.MASTER_OPEN.forTitle().formatted(ENTITY_TITLE);
    public final static String DESC = Template.MASTER_OPEN.forDesc().formatted(ENTITY_TITLE);
}
