package ua.com.fielden.platform.equery;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.equery.interfaces.IEntityMapper;

public class ModelResult {
    private Class resultType;
    private String sql;
    private Map<String, ColumnInfo> primitivedPropsAliases; // just names of the properties
    private Map<String, IEntityMapper> entityPropsMappers; // names and types of the properties, which are entities
    private List<ColumnInfo> finals;

    public ModelResult(final Class resultType, final String sql, final Map<String, ColumnInfo> primitivePropsAliases, final Map<String, IEntityMapper> entityPropsAliases, final List<ColumnInfo> finals) {
	this.resultType = resultType;
	this.sql = sql;
	this.primitivedPropsAliases = primitivePropsAliases;
	this.entityPropsMappers = entityPropsAliases;
	this.finals = finals;
    }

    public ModelResult(final List<ModelResult> modelResults) {
	final StringBuffer sb = new StringBuffer();

	for (final Iterator<ModelResult> iterator = modelResults.iterator(); iterator.hasNext();) {
	    final ModelResult modelResult = iterator.next();
	    sb.append(modelResult.getSql());
	    if (iterator.hasNext()) {
		sb.append("\n   UNION ALL\n   ");
	    }
	}

	this.sql = sb.toString();
	this.primitivedPropsAliases = modelResults.get(0).getPrimitivePropsAliases();
	this.entityPropsMappers = modelResults.get(0).getEntityPropsMappers();
	this.resultType = modelResults.get(0).getResultType();
    }

    public String getSql() {
	return sql;
    }

    public Map<String, ColumnInfo> getPrimitivePropsAliases() {
	return primitivedPropsAliases;
    }

    public Map<String, IEntityMapper> getEntityPropsMappers() {
	return entityPropsMappers;
    }

    public Class getResultType() {
	return resultType;
    }

    public boolean isIdOnlyQuery() {
	return primitivedPropsAliases.size() + entityPropsMappers.size() == 0;
    }

    public List<ColumnInfo> getFinals() {
        return finals;
    }
}