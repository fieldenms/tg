package ua.com.fielden.platform.eql.stage3.sources;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractSource3 implements ISource3 {
    public final String sqlAlias;
    private final Integer id;
    private final Map<String, String> columns;

    protected AbstractSource3(final String sqlAlias, final Integer id, final Map<String, String> columns) {
        this.sqlAlias = sqlAlias;
        this.id = id;
        this.columns = Map.copyOf(columns);
    }
    
    @Override
    public Integer id() {
        return id;
    }
    
    @Override
    public String column(final String colName) {
         return sqlAlias + "." + columns.get(colName);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        //result = prime * result + sqlAlias.hashCode();
        //result = prime * result + columns.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof AbstractSource3)) {
            return false;
        }
        
        final AbstractSource3 other = (AbstractSource3) obj;
        
        return 
                //Objects.equals(sqlAlias, other.sqlAlias) && 
                Objects.equals(id, other.id);// && Objects.equals(columns, other.columns);
    }
}
