package ua.com.fielden.platform.eql.dbschema;

/// Represents a column index definition, specifying the sort [Order]
/// (ascending or descending) for a database column within an index.
///
/// @param order the sort order of the column in the index
///
record ColumnIndex(Order order) {

    enum Order {
        ASC, DESC;
    }

}
