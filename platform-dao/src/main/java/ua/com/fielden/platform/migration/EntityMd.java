package ua.com.fielden.platform.migration;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A record to represent migration-related entity meta-data.  
 *
 * @author TG Team
 *
 */
record EntityMd(String tableName, List<PropMd> props) {
}
