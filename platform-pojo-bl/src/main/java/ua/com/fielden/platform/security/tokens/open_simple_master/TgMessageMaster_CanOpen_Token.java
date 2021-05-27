package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;


/**
 * A security token for entity {@link TgMessage} to guard MASTER_OPEN.
 * 
 * @author TG Team
 */
public class TgMessageMaster_CanOpen_Token
    implements ISecurityToken
{
    private final static String ENTITY_TITLE = (TitlesDescsGetter.getEntityTitleAndDesc(TgMessage.class).getKey()+" Master");
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), ENTITY_TITLE);
}
