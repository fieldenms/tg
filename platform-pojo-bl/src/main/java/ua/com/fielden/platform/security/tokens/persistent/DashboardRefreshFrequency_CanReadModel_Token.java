package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;

import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link DashboardRefreshFrequency} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class DashboardRefreshFrequency_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = format(READ_MODEL.forTitle(), DashboardRefreshFrequency.ENTITY_TITLE);
    public final static String DESC = format(READ_MODEL.forDesc(), DashboardRefreshFrequency.ENTITY_TITLE);
}