package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.SortedSet;

public class QueryModelResult {
    private Class resultType;
    private String sql;
    private Map<String, Object> paramValues;
    private final SortedSet<ResultPropertyInfo> yieldedPropsInfo;

    public QueryModelResult(final Class resultType, final String sql, final SortedSet<ResultPropertyInfo> yieldedPropsInfo, final Map<String, Object> paramValues) {
	this.resultType = resultType;
	this.sql = sql;
	this.paramValues = paramValues;
	this.yieldedPropsInfo = yieldedPropsInfo;
    }

    public Class getResultType() {
        return resultType;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public SortedSet<ResultPropertyInfo> getYieldedPropsInfo() {
	return yieldedPropsInfo;
    }

    public static class ResultPropertyInfo implements Comparable<ResultPropertyInfo> {
	private String name;
	private String sqlAlias;
	private Class type;

	@Override
	public String toString() {
	    // TODO Auto-generated method stub
	    return "RPI\n name = " + name + "\n sqlAlias = " + sqlAlias + "\n type = " + (type != null ? type.getSimpleName() : " --- ");
	}

	public ResultPropertyInfo(final String name, final String sqlAlias, final Class type) {
	    this.name = name;
	    this.sqlAlias = sqlAlias;
	    this.type = type;
	}

	public String getSqlAlias() {
	    return sqlAlias;
	}

	public String getName() {
	    return name;
	}

	public Class getType() {
	    return type;
	}

	@Override
	public int compareTo(final ResultPropertyInfo o) {
	    return name.compareTo(o.name);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    result = prime * result + ((sqlAlias == null) ? 0 : sqlAlias.hashCode());
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (!(obj instanceof ResultPropertyInfo)) {
		return false;
	    }
	    final ResultPropertyInfo other = (ResultPropertyInfo) obj;
	    if (name == null) {
		if (other.name != null) {
		    return false;
		}
	    } else if (!name.equals(other.name)) {
		return false;
	    }
	    if (sqlAlias == null) {
		if (other.sqlAlias != null) {
		    return false;
		}
	    } else if (!sqlAlias.equals(other.sqlAlias)) {
		return false;
	    }
	    if (type == null) {
		if (other.type != null) {
		    return false;
		}
	    } else if (!type.equals(other.type)) {
		return false;
	    }
	    return true;
	}
    }
}