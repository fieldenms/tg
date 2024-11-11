package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.sample.domain.TgNote;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgNote} to guard Read Model.
 *
 * @author TG Team
 */
public class TgNote_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ_MODEL.forTitle(), TgNote.ENTITY_TITLE);
    public final static String DESC = format(Template.READ_MODEL.forDesc(), TgNote.ENTITY_TITLE);
}
