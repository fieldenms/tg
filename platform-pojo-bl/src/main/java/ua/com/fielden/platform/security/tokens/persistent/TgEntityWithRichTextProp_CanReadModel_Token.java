package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/**
 * A security token for entity {@link TgEntityWithRichTextProp} to guard Read Model.
 *
 * @author TG Team
 */
public class TgEntityWithRichTextProp_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ_MODEL.forTitle(), TgEntityWithRichTextProp.ENTITY_TITLE);
    public final static String DESC = format(Template.READ_MODEL.forDesc(), TgEntityWithRichTextProp.ENTITY_TITLE);
}
