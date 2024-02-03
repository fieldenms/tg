package ua.com.fielden.platform.eql.meta;

import java.util.Map;

/**
 * A structure to represent a database table used for the final SQL generation from an EQL query.
 * <p>
 * @param name -- A name of the table, where an entity is mapped to.
 * @param columns -- A map between entity property names and corresponding column names.
 *                   Names of properties that are components or union types include a dot (i.e., "cost.amount", "location.workshop").
 *                   This is because those lower level attributes get mapped to database columns.
 *
 * @author TG Team
 */
public record EqlTable (String name, Map<String, String> columns) {
}