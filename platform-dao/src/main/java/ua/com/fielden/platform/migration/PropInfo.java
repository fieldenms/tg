package ua.com.fielden.platform.migration;

import java.util.List;

/// Information about an entity property that needs to be migrated.
///
/// @param propName  property path
/// @param propType  type of the property
/// @param column  name of a column in the target database
/// @param utcType  `true` if the property represents a UTC date
/// @param indices  one or more integers that serve as indices into the array of a legacy data result set.
///                 If there are more than one index, then each index corresponds to a composite key member.
///                 Those composite key member indices follow an order that is the same as in [IdCache],
///                 legacy data result sets and target insert statements.
///
record PropInfo(String propName, Class<?> propType, String column, boolean utcType, List<Integer> indices) {
}
