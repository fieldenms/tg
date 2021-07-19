package ua.com.fielden.platform.security.tokens.open_simple_master;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.MASTER_OPEN;

import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link DashboardRefreshFrequency} to guard MASTER_OPEN.
 * 
 * @author TG Team
 */
public class DashboardRefreshFrequencyMaster_CanOpen_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(DashboardRefreshFrequency.class).getKey() + " Master";
    public final static String TITLE = format(MASTER_OPEN.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(MASTER_OPEN.forDesc(), ENTITY_TITLE);
}