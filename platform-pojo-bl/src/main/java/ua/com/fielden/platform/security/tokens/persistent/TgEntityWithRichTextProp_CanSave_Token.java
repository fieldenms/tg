package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgEntityWithRichTextProp} to guard Save.
 *
 * @author TG Team
 */
public class TgEntityWithRichTextProp_CanSave_Token implements ISecurityToken {
    public final static String TITLE = format(Template.SAVE.forTitle(), TgEntityWithRichTextProp.ENTITY_TITLE);
    public final static String DESC = format(Template.SAVE.forDesc(), TgEntityWithRichTextProp.ENTITY_TITLE);
}