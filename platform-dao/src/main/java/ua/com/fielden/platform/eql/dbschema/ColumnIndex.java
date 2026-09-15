package ua.com.fielden.platform.eql.dbschema;

import java.util.Optional;

/// Represents a column index definition, specifying the sort [Order]
/// (ascending or descending) for a database column within an index.
///
/// @param order the sort order of the column in the index
/// @param maybeExpression an optional expression on which the index is defined (supported by PosgtreSQL)
///
record ColumnIndex(Order order, Optional<String> maybeExpression) {

    enum Order {
        ASC, DESC;
    }

}
