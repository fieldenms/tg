package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.DELETE;

import ua.com.fielden.platform.dashboard.Duration;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link Duration} to guard DELETE.
 * 
 * @author TG Team
 */
public class Duration_CanDelete_Token implements ISecurityToken {
    public final static String TITLE = format(DELETE.forTitle(), Duration.ENTITY_TITLE);
    public final static String DESC = format(DELETE.forDesc(), Duration.ENTITY_TITLE);
}