package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery.PropTree;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourcesEnhancer {
    public Map<String, Set<String>> determinePropGroups(final Set<String> props) {
	final Map<String, Set<String>> result = new HashMap<String, Set<String>>();
	for (final String dotNotatedPropName : props) {
	    final Pair<String, String> splitOfProperty = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	    Set<String> propGroup = result.get(splitOfProperty.getKey());
	    if (propGroup == null) {
		propGroup = new HashSet<String>();
		result.put(splitOfProperty.getKey(), propGroup);
	    }
	    if (splitOfProperty.getValue() != null) {
		propGroup.add(splitOfProperty.getValue());
	    }
	}

	return result;
    }

    private String composeAlias(final String predecessorAlias, final String propAlias) {
	return predecessorAlias == null ? propAlias : predecessorAlias + "." + propAlias;
    }

    public Set<PropTree> produceSourcesTree(final IEntQuerySourceDataProvider entQrySourceDataProvider, final String parentAlias, final boolean parentLeftJoin, final Set<String> props) {
	final Set<PropTree> result = new HashSet<PropTree>();
	final Map<String, Set<String>> propGrops = determinePropGroups(props);

	for (final Map.Entry<String, Set<String>> entry : propGrops.entrySet()) {
	    if (entry.getValue().size() > 0) {
		final Class propType = entQrySourceDataProvider.propType(entry.getKey());

		if (entQrySourceDataProvider.parentType().equals(EntityAggregates.class)) {
		    //throw new IllegalStateException("Prop of type EntityAggregates is not possible/supported");
		}
		final boolean propLeftJoin = parentLeftJoin || !(EntityUtils.isPropertyPartOfKey(entQrySourceDataProvider.parentType(), entry.getKey()) || EntityUtils.isPropertyRequired(entQrySourceDataProvider.parentType(), entry.getKey()));


		if (EntityUtils.isPersistedEntityType(propType)) {
		    result.add(new PropTree(entry.getKey(), composeAlias(parentAlias, entry.getKey()), propType, propLeftJoin, produceSourcesTree(new RealEntityTypeDataProvider(propType), composeAlias(parentAlias, entry.getKey()), propLeftJoin, entry.getValue())));
		}
	    }
	}
	return result;
    }
}