package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.SAVE;

import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link DashboardRefreshFrequency} to guard SAVE.
 * 
 * @author TG Team
 */
public class DashboardRefreshFrequency_CanSave_Token implements ISecurityToken {
    public final static String TITLE = format(SAVE.forTitle(), DashboardRefreshFrequency.ENTITY_TITLE);
    public final static String DESC = format(SAVE.forDesc(), DashboardRefreshFrequency.ENTITY_TITLE);
}