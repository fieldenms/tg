package ua.com.fielden.platform.migration;

import java.util.List;

/**
 * A record to represent migration-related entity meta-data.  
 *
 * @author TG Team
 *
 */
record EntityMd(String tableName, List<PropMd> props) {
}
