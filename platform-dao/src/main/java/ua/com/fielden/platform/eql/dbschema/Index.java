package ua.com.fielden.platform.eql.dbschema;

/// Represents an index definition for a database table.
///
/// An index is described independently of any column definition, which makes it possible to index things other than
/// a plain column — refer to [Expression].
///
public sealed interface Index {

    /// The name of this index.
    ///
    String name();

    /// The sort order of the indexed column or expression.
    ///
    Order order();

    /// An index on a single table column.
    ///
    /// @param name  the index name
    /// @param order  the sort order of the column in the index
    /// @param column  the name of the indexed column
    ///
    record Column (String name, Order order, String column)
    implements Index {}

    static Index Column(final String name, final Order order, final String column) {
        return new Column(name, order, column);
    }

    /// An index on an SQL expression (supported by PostgreSQL).
    ///
    /// @param name  the index name
    /// @param order  the sort order of the expression in the index
    /// @param expression  the indexed SQL expression
    ///
    record Expression (String name, Order order, String expression)
    implements Index {}

    static Index Expression(final String name, final Order order, final String expression) {
        return new Expression(name, order, expression);
    }

    /// A sort order, ascending or descending, of an indexed column or expression.
    ///
    enum Order {
        ASC, DESC;
    }

}
