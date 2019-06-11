package ua.com.fielden.platform.eql.stage3.elements;

public class Column {
    public final String name;

    public Column(final String name) {
        this.name = name;
    }
    
    public String sql() {
        return name;
    }
}