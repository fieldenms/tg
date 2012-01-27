package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.query.model.elements.EntQuery.PropTree;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourcesEnhancer {

    /**
     *
     * @param props
     * @return
     */
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

    public SortedSet<PropTree> produceSourcesTree(final IEntQuerySource entQrySourceDataProvider, final boolean parentLeftJoin, final Set<String> props) {
	final SortedSet<PropTree> result = new TreeSet<PropTree>();

	for (final Map.Entry<String, Set<String>> entry : determinePropGroups(props).entrySet()) {
	    if (entry.getValue().size() > 0) {
		final Class propType = entQrySourceDataProvider.propType(entry.getKey());

		final boolean propLeftJoin = parentLeftJoin
			|| !(EntityUtils.isPropertyPartOfKey(entQrySourceDataProvider.sourceType(), entry.getKey()) || EntityUtils.isPropertyRequired(entQrySourceDataProvider.sourceType(), entry.getKey()));

		if (EntityUtils.isPersistedEntityType(propType)) {
		    result.add(new PropTree(new EntQuerySourceAsEntity(propType, composeAlias(entQrySourceDataProvider.getAlias(), entry.getKey()), true), propLeftJoin, produceSourcesTree(new EntQuerySourceAsEntity(propType, composeAlias(entQrySourceDataProvider.getAlias(), entry.getKey()), true), propLeftJoin, entry.getValue())));
		}
	    }
	}

	return result;
    }
}