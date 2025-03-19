package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextRef;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgEntityWithRichTextRef} to guard Save.
 *
 * @author TG Team
 */
public class TgEntityWithRichTextRef_CanSave_Token implements ISecurityToken {
    public final static String TITLE = format(Template.SAVE.forTitle(), TgEntityWithRichTextRef.ENTITY_TITLE);
    public final static String DESC = format(Template.SAVE.forDesc(), TgEntityWithRichTextRef.ENTITY_TITLE);
}
