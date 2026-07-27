package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

public class TgPersistentEntityWithProperties_CanRead_moneyProp_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ_PROP.forTitle(), TgPersistentEntityWithProperties.ENTITY_TITLE, "moneyProp");
    public final static String DESC = format(Template.READ_PROP.forDesc());
}
