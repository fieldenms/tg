package ua.com.fielden.platform.migration;

import java.util.List;

/**
 * A record to represent migration-related property meta-data.  
 *
 * @author TG Team
 *
 */
record PropMd(String name, Class<?> type, String column, boolean required, boolean utcType, List<String> leafProps) {
}
