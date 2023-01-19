package ua.com.fielden.platform.eql.stage3.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.Table;

public class Source3BasedOnTable extends AbstractSource3 {
    public final Table table;
    
    public Source3BasedOnTable(final Table table, final Integer id, final int sqlId) {
        super("T_" + sqlId, id, table.columns);
        this.table = table;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return table.name + " AS " + sqlAlias;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "QrySource3BasedOnTable [" + table.name +"]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + table.name.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof Source3BasedOnTable)) {
            return false;
        }
        
        final Source3BasedOnTable other = (Source3BasedOnTable) obj;
        
        return Objects.equals(table, other.table);
    }
}