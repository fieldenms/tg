package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.query.generation.elements.EntQuery.PropTree;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourcesEnhancer {

    public Map<String, Set<String>> determinePropGroups(final Set<String> dotNotatedPropNames) {
	final Map<String, Set<String>> result = new HashMap<String, Set<String>>();
	for (final String dotNotatedPropName : dotNotatedPropNames) {
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

    public SortedSet<PropTree> produceSourcesTree(final IEntQuerySource entQrySource, final boolean parentLeftJoinLegacy, final Set<String> props, final EntQuery holder) {
	final SortedSet<PropTree> result = new TreeSet<PropTree>();

	for (final Map.Entry<String, Set<String>> entry : determinePropGroups(props).entrySet()) {
	    if (entry.getValue().size() > 0) {
		final Class propType = entQrySource.propType(entry.getKey());

		final boolean propLeftJoin = parentLeftJoinLegacy
			|| !(EntityUtils.isPropertyPartOfKey(entQrySource.sourceType(), entry.getKey()) || EntityUtils.isPropertyRequired(entQrySource.sourceType(), entry.getKey()));

		if (EntityUtils.isPersistedEntityType(propType)) {
		    result.add(new PropTree(new EntQuerySourceAsEntity(propType, composeAlias(entQrySource.getAlias(), entry.getKey()), true), propLeftJoin, produceSourcesTree(new EntQuerySourceAsEntity(propType, composeAlias(entQrySource.getAlias(), entry.getKey()), true), propLeftJoin, entry.getValue(), holder), holder));
		}
	    }
	}

	return result;
    }
}