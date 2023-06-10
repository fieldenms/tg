package ua.com.fielden.platform.migration;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A record to represent migration-related entity meta-data.  
 *
 * @author TG Team
 *
 */
public record EntityMd(@Nonnull String tableName, @Nonnull List<PropMd> props) {
}