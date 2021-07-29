package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.DELETE;

import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link DashboardRefreshFrequency} to guard DELETE.
 * 
 * @author TG Team
 */
public class DashboardRefreshFrequency_CanDelete_Token implements ISecurityToken {
    public final static String TITLE = format(DELETE.forTitle(), DashboardRefreshFrequency.ENTITY_TITLE);
    public final static String DESC = format(DELETE.forDesc(), DashboardRefreshFrequency.ENTITY_TITLE);
}