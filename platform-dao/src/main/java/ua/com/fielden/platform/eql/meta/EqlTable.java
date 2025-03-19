package ua.com.fielden.platform.eql.meta;

import java.util.Map;

/**
 * A structure to represent a database table used for the final SQL generation from an EQL query.
 * <p>
 * @param name  The name of the table, where an entity is mapped to.
 * @param columns  A map from property paths to column names.
 *                 <ul>
 *                   <li> If a property is component-typed, the map contains entries for each component, with each key representing a full path to the component
 *                        (e.g., the set of keys for property {@code note : RichText} is {@code {note.coreText, note.formattedText}}).
 *                   <li> If a property is union-typed, the map contains entries for each union member, with each key representing a full path to the member
 *                        (e.g., the set of keys for union-typed property {@code location : Location}, where union members are {@code workshop, station},
 *                        is {@code {location.workshop, location.station}}).
 *                   <li> Otherwise, the map contains a single entry for the property, with the key equal to the property's name.
 *                 </ul>
 *
 * @author TG Team
 */
public record EqlTable (String name, Map<String, String> columns) {

    /**
     * Returns the name of a column that the specified property is mapped onto.
     * <p>
     * It is an error if the specified property doesn't exist in the entity type corresponding to this table, or if it is not mapped onto a column.
     *
     * @param property  a property path that fits the description of keys in {@link #columns()}
     */
    public String getColumn(final CharSequence property) {
        final var column = columns.get(property.toString());
        if (column == null) {
            throw new IllegalArgumentException(String.format("Entity table [%s] doesn't contain a column for property [%s]", name, property));
        }
        return column;
    }

}
