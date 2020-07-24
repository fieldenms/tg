package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Map;

public class Table {
    public final String name;
    public final Map<String, String> columns;

    public Table(final String name, final Map<String, String> columns) {
        this.name = name;
        this.columns = columns;
    }
}