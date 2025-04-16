package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgNote;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgNote} to guard master open.
 *
 * @author TG Team
 */
public class TgNoteMaster_CanOpen_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = (TitlesDescsGetter.getEntityTitleAndDesc(TgNote.class).getKey()+" Master");
    public final static String TITLE = format(Template.MASTER_OPEN.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(Template.MASTER_OPEN.forDesc(), ENTITY_TITLE);
}
