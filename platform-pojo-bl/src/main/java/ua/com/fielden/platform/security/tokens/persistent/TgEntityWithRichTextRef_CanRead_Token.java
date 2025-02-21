package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextRef;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgEntityWithRichTextRef} to guard Read.
 *
 * @author TG Team
 */
public class TgEntityWithRichTextRef_CanRead_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ.forTitle(), TgEntityWithRichTextRef.ENTITY_TITLE);
    public final static String DESC = format(Template.READ.forDesc(), TgEntityWithRichTextRef.ENTITY_TITLE);
}
