package ua.com.fielden.platform.entity.query.model.structure;


public class SubQueryModelResult {

    private Class resultType;
    private String sql;

    public SubQueryModelResult(final Class resultType, final String sql) {
	this.resultType = resultType;
	this.sql = sql;
    }

    public Class getResultType() {
        return resultType;
    }

    public String getSql() {
        return sql;
    }
}
