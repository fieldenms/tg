package ua.com.fielden.platform.eql.stage3.elements;

public class QueryAsQuerySource {
    public final SqlQuery query;
    public final Long uid;
    
    public QueryAsQuerySource(final SqlQuery query, final Long uid) {
        this.query = query;
        this.uid = uid;
    }
    
    public String sqlAlias() {
        return "T_" + uid;
    }
    
    public String sql() {
        return "(" + query.sql() + ") AS " + sqlAlias();
    }
}
