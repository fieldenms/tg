package ua.com.fielden.platform.equery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IJoinEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.tokens.main.JoinConditions;
import ua.com.fielden.platform.equery.tokens.properties.PropertyOrigin;

public class RootEntityMapper {
    private List<JoinConditions> joins;
    private boolean returned;
    private List<String> resultantPropertiesAliases;
    private Map<String, IJoinEntityMapper> mappers = new HashMap<String, IJoinEntityMapper>();

    private IMappingExtractor mappingExtractor;
    private AliasNumerator aliasNumerator;
    private RootEntityMapper masterModelMapper;

    public RootEntityMapper(final List<JoinConditions> joins, final boolean returned, final IMappingExtractor mappingExtractor, final RootEntityMapper masterModelMapper, final List<String> resultantPropertiesAliases, final AliasNumerator aliasNumerator) {
	this.joins = joins;
	this.returned = returned;
	this.mappingExtractor = mappingExtractor;
	this.masterModelMapper = masterModelMapper;
	this.resultantPropertiesAliases = resultantPropertiesAliases;
	this.aliasNumerator = aliasNumerator;

	// producing mappers for joins
	for (final JoinConditions join : joins) {
	    if (join.getQuerySource().isEntityTypeBased() && !join.getQuerySource().isUnionEntityTypeBased()) {
		    mappers.put(join.getAlias(), new RealEntityMapper(resultantPropertiesAliases, join.getQuerySource().getEntityType(), mappingExtractor, aliasNumerator));
	    } else {
		final List<ModelResult> sourceModelResults = new ArrayList<ModelResult>();
		for (final IQueryModel sourceModel : join.getQuerySource().getModels()) {
		    ((QueryModel) sourceModel).setAliasNumerator(aliasNumerator);
		    sourceModelResults.add(sourceModel.getModelResult(mappingExtractor));
		}
		if (join.getQuerySource().isUnionEntityTypeBased()) {
		    mappers.put(join.getAlias(), new SyntheticMapper(resultantPropertiesAliases, new ModelResult(sourceModelResults), join.getQuerySource().getEntityType(),  mappingExtractor, aliasNumerator));
		} else {
		    mappers.put(join.getAlias(), new SyntheticMapper(resultantPropertiesAliases, new ModelResult(sourceModelResults), null,  mappingExtractor, aliasNumerator));
		}

	    }
	}
    }

    private String removeAlias(final String propertyDotName, final String alias) {
	return alias == null ? propertyDotName : (alias.length() < propertyDotName.length() ? propertyDotName.substring(alias.length() + 1) : "id");
    }

    public String getAliasedProperty(final PropertyOrigin propertyOrigin, final String propertyDotName) {
	if ("*".equalsIgnoreCase(propertyDotName)) {
	    return "*";
	}

	// handling cases when ordering is done by calculated in the current select clause fields
	if (PropertyOrigin.ORDERBY.equals(propertyOrigin) && resultantPropertiesAliases.contains(propertyDotName)) {
	    return propertyDotName;
	}

	RootEntityMapper it = findRootMapperInHierarchy(propertyDotName.split("\\.")[0]);
	final String alias = it == null ? null : propertyDotName.split("\\.")[0];
	final String unaliasedValue = removeAlias(propertyDotName, alias);
	it = it != null ? it : this;

	final String result = it.getMappers().get(alias).getAliasedProperty(propertyOrigin, unaliasedValue);
	if (result != null) {
	    return result;
	} else {
	    throw new RuntimeException("Can't process property: " + propertyDotName);
	}

    }

    public IEntityMapper getParentMapperForEntityPropertyInSelect(final String propName) {
	RootEntityMapper it = findRootMapperInHierarchy(propName.split("\\.")[0]);
	final String alias = it == null ? null : propName.split("\\.")[0];
	final String unaliasedValue = removeAlias(propName, alias);
	it = it != null ? it : this;

	return it.getMappers().get(alias).getParentMapperForEntityPropertyInSelect(unaliasedValue);
    }

    /**
     *
     */
    private RootEntityMapper findRootMapperInHierarchy(final String alias) {
	if (mappers.containsKey(alias)) {
	    return this;
	} else if (getMasterModelMapper() != null) {
	    return getMasterModelMapper().findRootMapperInHierarchy(alias);
	} else {
	    return null;
	}
    }

    public IJoinEntityMapper getFirstJoin() {
	return mappers.get(joins.get(0).getAlias());
    }

    public String getFromClauseSql() {
	final Iterator<JoinConditions> it = joins.iterator();
	final StringBuffer sb = new StringBuffer();

	sb.append("\n   FROM " + mappers.get(it.next().getAlias()).getFromClauseSql());

	while (it.hasNext()) {
	    final JoinConditions join = it.next();
	    sb.append(join.isLeft() ? " LEFT JOIN " : " INNER JOIN ");
	    sb.append(mappers.get(join.getAlias()).getFromClauseSql());
	    sb.append(" ON ");
	    sb.append(join.getOnGroup().getSql(this));
	    sb.append(" ");
	}

	return sb.toString();
    }

    public IMappingExtractor getMappingExtractor() {
	return mappingExtractor;
    }

    public boolean isReturned() {
	return returned;
    }

    public RootEntityMapper getMasterModelMapper() {
	return masterModelMapper;
    }

    public AliasNumerator getAliasNumerator() {
        return aliasNumerator;
    }

    public Map<String, IJoinEntityMapper> getMappers() {
        return mappers;
    }
}
