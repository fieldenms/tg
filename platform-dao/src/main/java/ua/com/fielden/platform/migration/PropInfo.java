package ua.com.fielden.platform.migration;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Information about entity property that needs to be migrated.
 *
 * @param propName -- full dot-notated prop name that corresponds to a column (see field column below)
 * @param propType -- in case of Entity Type it is used for lookups in the cache of entities Map of ids to business key values
 * @param column -- column that prop is mapped to in the target database
 * @param utcType -- helper flag specific for handling dates in UTC
 * @param indices -- One or more indices that correspond to the order of field mappings in a legacy data result set.
 *                   If it contains more than one index, then each index corresponds to an individual composite key member.
 *                   Those composite key member indices have a specific order that is the same as in the IdCache, the legacy data result set and the target insert statements.
 * @author TG Team
 *
 */
public record PropInfo(@Nonnull String propName, @Nonnull Class<?> propType, @Nonnull String column, boolean utcType, @Nonnull List<Integer> indices) {
}