package ua.com.fielden.platform.equery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class ReturnedModelResult extends ModelResult {
    private Map<String, TypesInfo> scalarAliases;
    private final Map<String, Object> paramValues = new HashMap<String, Object>();

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public ReturnedModelResult(final ModelResult modelResult, final Map<String, Object> userParamValues, final List<QueryParameter> internalParams) {
	super(modelResult.getResultType(), modelResult.getSql(), modelResult.getPrimitivePropsAliases(), modelResult.getEntityPropsMappers(), modelResult.getFinals());
	this.scalarAliases = getScalarAliases(getFinals());
	for (final QueryParameter queryParameter : internalParams) {
	    paramValues.put(queryParameter.getParamName(), queryParameter.getParamValue());
	}
	paramValues.putAll(userParamValues); // user params with null as values (in paramValues map) will get their real values
    }

    public Map<String, TypesInfo> getScalarAliases() {
	return scalarAliases;
    }

    private Map<String, TypesInfo> getScalarAliases(final List<ColumnInfo> finals) {
	/////////////////////////////////// GETTING SCALAR ALIASES ////////////////////////////

	final Map<String, TypesInfo> scalarAliases = new TreeMap<String, TypesInfo>();

	for (final ColumnInfo tupleInf : finals) {
	    if (!(tupleInf instanceof ColumnInfoForPrimProp)) {
		scalarAliases.put(tupleInf.getColumnAlias(), null); // TODO return
	    } else {
		Class hibType = null;
		Class hibEntityType = null;
		try {
		    hibType = Class.forName(((ColumnInfoForPrimProp) tupleInf).getHibernateType());
		    hibEntityType = tupleInf instanceof ColumnInfoForEntityProp ? Class.forName(((ColumnInfoForEntityProp) tupleInf).getHibernateEntityType())
			    : null;
		} catch (final ClassNotFoundException e) {
		    throw new RuntimeException(e);
		}

		scalarAliases.put(tupleInf.getColumnAlias(), new TypesInfo(hibType, hibEntityType, tupleInf instanceof ColumnInfoForUnionEntityProp ? ((ColumnInfoForUnionEntityProp) tupleInf).getPolymorphicTypes()
			: new HashMap<String, Class>()));
	    }
	}

	return scalarAliases;
    }

    public static class TypesInfo {
	Class hibType;
	Class hibEntityType;
	Map<String, Class> hibPolymorhicTypes = new HashMap<String, Class>();

	public TypesInfo(final Class hibType, final Class hibEntityType, final Map<String, Class> hibPolymorhicTypes) {
	    this.hibEntityType = hibEntityType;
	    this.hibType = hibType;
	    this.hibPolymorhicTypes.putAll(hibPolymorhicTypes);
	}

	public Class getHibType() {
	    return hibType;
	}

	public Class getHibEntityType() {
	    return hibEntityType;
	}

	public Map<String, Class> getHibPolymorhicTypes() {
	    return hibPolymorhicTypes;
	}
    }
}