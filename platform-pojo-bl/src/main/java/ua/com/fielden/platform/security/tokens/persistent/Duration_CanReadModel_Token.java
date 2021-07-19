package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;

import ua.com.fielden.platform.dashboard.Duration;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link Duration} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class Duration_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = format(READ_MODEL.forTitle(), Duration.ENTITY_TITLE);
    public final static String DESC = format(READ_MODEL.forDesc(), Duration.ENTITY_TITLE);
}