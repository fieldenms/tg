package ua.com.fielden.platform.entity.query.model.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class QueryModelResult extends SubQueryModelResult {
    private final Map<String, Object> paramValues = new HashMap<String, Object>();
    private final SortedMap<String, ResultPropertyInfo> yieldedPropsInfo = new TreeMap<String, ResultPropertyInfo>();

    public QueryModelResult(final Class resultType, final String sql) {
	super(resultType, sql);
    }

    public Map<String, Object> getParamValues() {
	return paramValues;
    }

    public SortedMap<String, ResultPropertyInfo> getYieldedPropsInfo() {
	return yieldedPropsInfo;
    }

    public static class ResultPropertyInfo {
	private String name;
	private String sqlAlias;
	private Class type; // needed for cases where query with no result type set is used as source query; in this situation java type will allow to determine whether given property is entity and needs explicit joins for its subproperties access.

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
