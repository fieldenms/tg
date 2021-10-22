package ua.com.fielden.platform.migration;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A record to represent migration-related property meta-data.  
 *
 * @author TG Team
 *
 */
public record PropMd(@Nonnull String name, @Nonnull Class<?> type, @Nonnull String column, boolean required, boolean utcType, @Nonnull List<String> leafProps) {
}