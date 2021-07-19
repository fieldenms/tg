package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.READ;

import ua.com.fielden.platform.dashboard.DurationUnit;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link DurationUnit} to guard READ.
 * 
 * @author TG Team
 */
public class DurationUnit_CanRead_Token implements ISecurityToken {
    public final static String TITLE = format(READ.forTitle(), DurationUnit.ENTITY_TITLE);
    public final static String DESC = format(READ.forDesc(), DurationUnit.ENTITY_TITLE);
}