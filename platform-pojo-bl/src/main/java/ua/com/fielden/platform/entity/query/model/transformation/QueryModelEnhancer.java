package ua.com.fielden.platform.entity.query.model.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryModelEnhancer {


    public Map<String, List<String>> groupByAlias(final Set<String> sourcesNames, final Set<String> propNames) {
	final Map<String, List<String>> result = new HashMap<String, List<String>>();

	for (final String alias : sourcesNames) {
	    result.put(alias, new ArrayList<String>());
	}

	final List<String> reversedAliases = Arrays.asList(sourcesNames.toArray(new String[]{}));
	Collections.sort(reversedAliases, Collections.reverseOrder());

	final List<String> propNamesMod = Arrays.asList(propNames.toArray(new String[0]));
	for (final String alias : reversedAliases) {
	    for (final Iterator<String> iterator = propNamesMod.iterator(); iterator.hasNext();) {
		final String propName = iterator.next();
		if (propName.toLowerCase().startsWith(alias.toLowerCase()) && propName.equalsIgnoreCase(alias)) {
		    result.get(alias).add(propName);
		    iterator.remove();
		}
	    }
	}

	return result;
    }
}
