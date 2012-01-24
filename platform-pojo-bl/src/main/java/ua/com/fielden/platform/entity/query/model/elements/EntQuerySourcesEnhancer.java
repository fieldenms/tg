package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.query.model.elements.EntQuery.PropTree;
import ua.com.fielden.platform.reflection.Finder;
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

    public Set<PropTree> doS(final Class parentEntityType, final String parentAlias, final boolean parentLeftJoin, final Set<String> props) {
	final Set<PropTree> result = new HashSet<PropTree>();
	final Map<String, Set<String>> propGrops = determinePropGroups(props);
	for (final Map.Entry<String, Set<String>> entry : propGrops.entrySet()) {
	    final Class propType = Finder.findFieldByName(parentEntityType, entry.getKey()).getType();
	    final boolean propLeftJoin = parentLeftJoin || !(EntityUtils.isPropertyPartOfKey(parentEntityType, entry.getKey()) || EntityUtils.isPropertyRequired(parentEntityType, entry.getKey()));
	    if (EntityUtils.isPersistedEntityType(propType) && entry.getValue().size() > 0) {
		result.add(new PropTree(entry.getKey(), composeAlias(parentAlias, entry.getKey()), propType, propLeftJoin, doS(propType, composeAlias(parentAlias, entry.getKey()), propLeftJoin, entry.getValue())));
	    }
	}
	return result;
    }
}
