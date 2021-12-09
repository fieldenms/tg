package ua.com.fielden.platform.eql.stage3.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.Table;

public class Source3BasedOnTable implements ISource3 {
    public final Table table;
    public final String id;
    public final int sqlId;
    
    public Source3BasedOnTable(final Table table, final String id, final int sqlId) {
        this.table = table;
        this.id = id;
        this.sqlId = sqlId;
    }

    @Override
    public String column(final String colName) {
         return table.columns.get(colName);
    }

    @Override
    public String sqlAlias() {
        return "T_" + sqlId;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return table.name + " AS " + sqlAlias();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "QrySource3BasedOnTable [" + table.name +"]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + table.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof Source3BasedOnTable)) {
            return false;
        }
        
        final Source3BasedOnTable other = (Source3BasedOnTable) obj;
        
        return Objects.equals(table, other.table) && Objects.equals(id, other.id);
    }
}