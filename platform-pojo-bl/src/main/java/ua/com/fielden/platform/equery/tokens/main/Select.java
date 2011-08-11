package ua.com.fielden.platform.equery.tokens.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.equery.ColumnInfo;
import ua.com.fielden.platform.equery.ColumnInfoForEntityProp;
import ua.com.fielden.platform.equery.ColumnInfoForPrimProp;
import ua.com.fielden.platform.equery.ModelResult;
import ua.com.fielden.platform.equery.QueryParameter;
import ua.com.fielden.platform.equery.ReturnedModelResult;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IJoinEntityMapper;
import ua.com.fielden.platform.equery.tokens.properties.SelectCalculatedProperty;

public final class Select implements IClon<Select> {
    private final ArrayList<SelectCalculatedProperty> selectCalculatedProps = new ArrayList<SelectCalculatedProperty>();

    public Select() {
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("SELECT\n");
	if (selectCalculatedProps.size() > 0) {
	    sb.append(" calc props:\n ");
	    for (final SelectCalculatedProperty prop : selectCalculatedProps) {
		sb.append("  " + prop);
	    }
	}

	// TODO complete

	return sb.toString();
    }

    private Select(final List<SelectCalculatedProperty> selectCalculatedProps) {
	this.selectCalculatedProps.addAll(selectCalculatedProps);
    }

    public List<SelectCalculatedProperty> getSelectCalculatedProps() {
	return selectCalculatedProps;
    }

    public Select clon() {
	final List<SelectCalculatedProperty> clonedSelectCalculatedProps = new ArrayList<SelectCalculatedProperty>();
	for (final SelectCalculatedProperty selectCalculatedProperty : selectCalculatedProps) {
	    clonedSelectCalculatedProps.add(selectCalculatedProperty.clon());
	}

	return new Select(clonedSelectCalculatedProps);
    }

    public Select clonWithoutSubqueries() {
	final List<SelectCalculatedProperty> clonedSelectCalculatedProps = new ArrayList<SelectCalculatedProperty>();
	for (final SelectCalculatedProperty selectCalculatedProperty : selectCalculatedProps) {
	    if (!selectCalculatedProperty.isModel()) {
		clonedSelectCalculatedProps.add(selectCalculatedProperty.clon());
	    }
	}

	return new Select(clonedSelectCalculatedProps);
    }

    public boolean noPropertiesSpecified() {
	return selectCalculatedProps.size() == 0;
    }

    private List<ColumnInfo> getColumnsResultSql(final IEntityMapper entityPropMapper) {
	final List<ColumnInfo> result = new ArrayList<ColumnInfo>();
	for (final Map.Entry<String, ColumnInfo> entry : entityPropMapper.getPropertiesColumns().entrySet()) {
	    result.add(entry.getValue());
	}
	for (final Map.Entry<String, IEntityMapper> entry : entityPropMapper.getSubMappers().entrySet()) {
	    result.addAll(getColumnsResultSql(entry.getValue()));
	}
	return result;
    }

    private String quoteContainingDots(final String unquotedValue) {
	return unquotedValue.contains(".") ? "\"" + unquotedValue + "\"" : unquotedValue;
    }

    private String getSql(final RootEntityMapper rootEntityMapper, final String baseSql, final List<String> groupByPropsSql, final List<String> orderByPropsSql, final List<ColumnInfo> finals) {
	final StringBuffer sb = new StringBuffer();
	sb.append("SELECT ");

	for (final Iterator<ColumnInfo> iterator = finals.iterator(); iterator.hasNext();) {
	    final ColumnInfo sql = iterator.next();
	    sb.append(sql.getSqlColumn());
	    sb.append(sql.getColumnAlias() == null ? "" : " AS " + sql.getColumnAlias());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}

	final String fromSql = rootEntityMapper.getFromClauseSql();
	sb.append(fromSql);
	sb.append(baseSql);

	//// Temporary solution

	final List<String> groupItemsTablesAliases = new ArrayList<String>();


	if (groupByPropsSql.size() > 0) {
	    sb.append("\n   GROUP BY ");

	    for (final ColumnInfo columnInfo : finals) {
		final String sqlColumn = columnInfo.getSqlColumn();
		final int dotPosition = sqlColumn.indexOf(".");
		//System.out.println("--------------- " + dotPosition + "  " + sqlColumn.substring(dotPosition + 1));
		if (dotPosition != -1 && sqlColumn.startsWith("T") /*&& sqlColumn.substring(dotPosition + 1).startsWith("C")*/ && !groupByPropsSql.contains(sqlColumn)) {
		    groupByPropsSql.add(sqlColumn);

		}
	    }

//	    for (final String columnInfo : groupByPropsSql) {
//		    System.out.println("  group  +  +  +  +  +  " + columnInfo);
//	    }


	    for (final Iterator<String> iterator = groupByPropsSql.iterator(); iterator.hasNext();) {
		final String property = iterator.next();
		sb.append(property);
		if (iterator.hasNext()) {
		    sb.append(", ");
		}
	    }
	}

	if (orderByPropsSql.size() > 0) {
	    sb.append("\n   ORDER BY ");

	    for (final Iterator<String> iterator = orderByPropsSql.iterator(); iterator.hasNext();) {
		final String property = iterator.next();
		sb.append(property);
		if (iterator.hasNext()) {
		    sb.append(", ");
		}
	    }
	}


	return sb.toString();
    }

    public ModelResult getResult(final RootEntityMapper rootEntityMapper, final Class resultType, final String baseSql, final List<String> groupByPropsSql, final List<String> orderByPropsSql) {
	try {
	    final List<ColumnInfo> finals = new ArrayList<ColumnInfo>();
	    final SortedMap<String, ColumnInfo> primitivePropsAliases = new TreeMap<String, ColumnInfo>(); // propName and column alias in sql
	    final SortedMap<String, IEntityMapper> entityPropsMappers = new TreeMap<String, IEntityMapper>();

	    for (final SelectCalculatedProperty selectCalculatedProperty : selectCalculatedProps) {
		if (!selectCalculatedProperty.isProp()) {
		    if (selectCalculatedProperty.isModel()) {
			final StringBuffer sqSb = new StringBuffer();
			final ModelResult sqModelResult = selectCalculatedProperty.getModels().get(0).getModelResult(rootEntityMapper.getMappingExtractor());
			sqSb.append("(");
			sqSb.append(sqModelResult.getSql());
			sqSb.append(")");
			final ColumnInfo ci = sqModelResult.isIdOnlyQuery() ? new ColumnInfoForEntityProp(sqSb.toString(), ((ColumnInfoForPrimProp) sqModelResult.getFinals().get(0)).getHibernateType(), sqModelResult.getResultType().getName(), null)
			: new ColumnInfo(sqSb.toString(), selectCalculatedProperty.getPropertyAlias(), null);

			ci.setColumnAlias(selectCalculatedProperty.getPropertyAlias()); // WATCH OUT

			primitivePropsAliases.put(selectCalculatedProperty.getPropertyAlias(), ci);
		    } else {
			primitivePropsAliases.put(selectCalculatedProperty.getPropertyAlias(), new ColumnInfo(selectCalculatedProperty.getSql(rootEntityMapper), selectCalculatedProperty.getPropertyAlias(), null));
		    }
		} else {
		    // return n -1 mapper and look for final mapper in its submappers
		    final IEntityMapper mapper = rootEntityMapper.getParentMapperForEntityPropertyInSelect(selectCalculatedProperty.getProp()/*getRawValue()*/);
		    if (mapper == null) {
			primitivePropsAliases.put(selectCalculatedProperty.getPropertyAlias(), new ColumnInfo(selectCalculatedProperty.getSql(rootEntityMapper), selectCalculatedProperty.getPropertyAlias(), null));
		    } else {
			final String[] props = selectCalculatedProperty.getProp()/*.getRawValue()*/.split("\\.");
			final String lastPropPart = props[props.length - 1];
			final IJoinEntityMapper mapp = rootEntityMapper.getMappers().get(lastPropPart);
			if (mapp != null) {
			    entityPropsMappers.put(selectCalculatedProperty.getPropertyAlias(), mapp);
			} else {
			    if (mapper.getSubMappers().containsKey(lastPropPart) && rootEntityMapper.getMasterModelMapper() == null) {
				entityPropsMappers.put(selectCalculatedProperty.getPropertyAlias(), mapper.getSubMappers().get(lastPropPart));
			    } else {
				final ColumnInfo pc = mapper.getPropertiesColumns().get(lastPropPart);
				if (pc == null || !(pc instanceof ColumnInfoForPrimProp/*ColumnInfoForEntityProp*/)) {
				    primitivePropsAliases.put(selectCalculatedProperty.getPropertyAlias(), new ColumnInfo(selectCalculatedProperty.getSql(rootEntityMapper), selectCalculatedProperty.getPropertyAlias(), null));
				} else {
				    // ??? INTERESTING CASE!
				    final ColumnInfo ci = pc.clon(selectCalculatedProperty.getSql(rootEntityMapper), null);
				    ci.setColumnAlias(selectCalculatedProperty.getPropertyAlias());// WATCH OUT
				    primitivePropsAliases.put(selectCalculatedProperty.getPropertyAlias(), ci);
				}
			    }
			}
		    }
		}
	    }

	    /////////////////////////////////////////////////////////////////////////////////////////
	    if (noPropertiesSpecified()) {
		if (rootEntityMapper.getMasterModelMapper() != null) {
		    finals.add(rootEntityMapper.getFirstJoin().getIdColumn());
		} else {
		    primitivePropsAliases.putAll(rootEntityMapper.getFirstJoin().getPropertiesColumns());
		    entityPropsMappers.putAll(rootEntityMapper.getFirstJoin().getSubMappers());
		}
	    }
	    /////////////////////////////////////////////////////////////////////////////////////////
	    /////////////////////////////////////////////////////////////////////////////////////////

	    finals.addAll(primitivePropsAliases.values());

	    for (final Map.Entry<String, IEntityMapper> entry : entityPropsMappers.entrySet()) {
		finals.addAll(getColumnsResultSql(entry.getValue()));
	    }

	    return new ModelResult(resultType, getSql(rootEntityMapper, baseSql, groupByPropsSql, orderByPropsSql, finals), primitivePropsAliases, entityPropsMappers, finals);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Couldn't produce result for model with:\n " + this + "\ndue to: " + e);
	}
    }


    public ReturnedModelResult getFinalResult(final RootEntityMapper rootEntityMapper, final Class resultType, final String baseSql, final List<String> groupByPropsSql, final List<String> orderByPropsSql,  final Map<String, Object> userParamValues, final List<QueryParameter> internalParams) {
	return new ReturnedModelResult(getResult(rootEntityMapper, resultType, baseSql, groupByPropsSql, orderByPropsSql), userParamValues, internalParams);
    }
}
