package ua.com.fielden.platform.eql.dbschema;

public sealed interface Index {

    record Column (String name, ColumnIndex.Order order, String column)
    implements Index {}

    static Index Column(final String name, final ColumnIndex.Order order, final String column) {
        return new Column(name, order, column);
    }

    record Expression (String name, ColumnIndex.Order order, String expression)
    implements Index {}

    static Index Expression(final String name, final ColumnIndex.Order order, final String expression) {
        return new Expression(name, order, expression);
    }

}
