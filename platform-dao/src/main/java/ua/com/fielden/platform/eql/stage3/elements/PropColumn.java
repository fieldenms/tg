package ua.com.fielden.platform.eql.stage3.elements;

public class PropColumn {
    public final TableAsQuerySource tqs;
    public final String colName;
    
    public PropColumn(final TableAsQuerySource tqs, final String colName) {
        this.tqs = tqs;
        this.colName = colName;
    }

    public String sql() {
        return tqs.sqlAlias() + "." + tqs.table.columns.get(colName).sql();
    }
    

}
