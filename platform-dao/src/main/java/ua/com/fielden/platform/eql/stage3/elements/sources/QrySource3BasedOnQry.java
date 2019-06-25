package ua.com.fielden.platform.eql.stage3.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.Column;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;

public class QrySource3BasedOnQry implements IQrySource3 {
    public final EntQuery3 qry;
    public final int contextId;
    
    public QrySource3BasedOnQry(final EntQuery3 qry, final int contextId) {
        this.qry = qry;
        this.contextId = contextId;
    }

    @Override
    public Column column(final String colName) {
         return null;//table.columns.get(colName);
    }

    @Override
    public String sqlAlias() {
        return "Q_" + contextId;
    }

    @Override
    public String sql() {
        return "(" + qry.sql() + ") AS " + sqlAlias();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + ((qry == null) ? 0 : qry.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof QrySource3BasedOnQry)) {
            return false;
        }
        
        final QrySource3BasedOnQry other = (QrySource3BasedOnQry) obj;
        
        return Objects.equals(qry, other.qry) && Objects.equals(contextId, other.contextId);
    }
}