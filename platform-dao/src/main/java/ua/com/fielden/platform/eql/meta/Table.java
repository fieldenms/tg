package ua.com.fielden.platform.eql.meta;

import java.util.Map;

public class Table {
    public final String name;
    public final Map<String, String> columns;

    public Table(final String name, final Map<String, String> columns) {
        this.name = name;
        this.columns = columns;
    }
}