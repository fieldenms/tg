package ua.com.fielden.platform.eql.stage3.elements;

public class TableAsQuerySource {
    public final Table table;
    public final Long uid;
    
    
    public TableAsQuerySource(final Table table, final Long uid) {
        this.table = table;
        this.uid = uid;
    }
    
    public String sqlAlias() {
        return "T_" + uid;
    }
    
    public String sql() {
        return table.name + " " + sqlAlias();
    }
}
