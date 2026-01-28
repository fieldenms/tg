package ua.com.fielden.platform.eql.dbschema;

record ColumnIndex (Order order) {

    enum Order {
        ASC, DESC;
    }

}
