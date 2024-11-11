package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.sample.domain.TgNote;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgNote} to guard Save.
 *
 * @author TG Team
 */
public class TgNote_CanSave_Token implements ISecurityToken {
    public final static String TITLE = format(Template.SAVE.forTitle(), TgNote.ENTITY_TITLE);
    public final static String DESC = format(Template.SAVE.forDesc(), TgNote.ENTITY_TITLE);
}
