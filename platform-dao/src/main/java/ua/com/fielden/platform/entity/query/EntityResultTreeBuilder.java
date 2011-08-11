package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

final class EntityResultTreeBuilder {
    private MappingsGenerator mappingsGenerator;
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder(final MappingsGenerator mappingsGenerator) {
	this.mappingsGenerator = mappingsGenerator;
    }

    /*DONE*/
    protected EntityTree buildTree(final Class resultType, final Collection<ResultPropertyInfo> properties) throws Exception {
	final EntityTree result = new EntityTree(resultType);

	final List<ResultPropertyInfo> singleProps = getFirstLevelSingleProps(properties);
	final Map<String, Collection<ResultPropertyInfo>> compositeProps = getFirstLevelCompositeProps(properties);

	for (final ResultPropertyInfo propInfo : singleProps) {
	    result.getSingles().put(new PropColumn(propInfo.getName(), propInfo.getSqlAlias(), mappingsGenerator.determinePropertyHibType(resultType, propInfo.getName()), mappingsGenerator.determinePropertyHibUserType(resultType, propInfo.getName())), index.getNext());
	}

	for (final Map.Entry<String, Collection<ResultPropertyInfo>> propEntry : compositeProps.entrySet()) {
	    final String subtreePropName = propEntry.getKey();
	    final Class subtreeType = determinePropertyType(resultType, subtreePropName);
	    final Class subtreeHibType = mappingsGenerator.determinePropertyHibCompositeUserType(resultType, subtreePropName);
	    result.getComposites().put(subtreePropName, buildTree((subtreeHibType == null ? subtreeType : subtreeHibType), propEntry.getValue()));
	}

	return result;
    }

    /*DONE*/
    /**
     * Extract explicit and implicit property groups; Explicit property groups are provided with property type.
     *
     * @param allProps
     * @return
     */
    private List<ResultPropertyInfo> getFirstLevelSingleProps(final Collection<ResultPropertyInfo> allProps) {
	final List<ResultPropertyInfo> result = new ArrayList<ResultPropertyInfo>();

	for (final ResultPropertyInfo prop : allProps) {
	    if (!prop.getName().contains(".")) {
		result.add(prop);
	    }
	}

	return result;
    }

    /*DONE*/
    private Map<String, Collection<ResultPropertyInfo>> getFirstLevelCompositeProps(final Collection<ResultPropertyInfo> allProps) {
	final Map<String, Collection<ResultPropertyInfo>> result = new HashMap<String, Collection<ResultPropertyInfo>>();

	for (final ResultPropertyInfo prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);
		if (!result.containsKey(group)) {
		    result.put(group, new ArrayList<ResultPropertyInfo>());
		}
		result.get(group).add(new ResultPropertyInfo(prop.getName().substring(firstDotIndex + 1), prop.getSqlAlias(), prop.getType()));
	    }
	}

	return result;
    }

    /*DONE*/
    private Class determinePropertyType(final Class<?> parentType, final String propName) {
	if (EntityAggregates.class.equals(parentType)) {
	    return null;
	} else {
	    return PropertyTypeDeterminator.determinePropertyType(parentType, propName);
	}
    }

    /*DONE*/
    private static class ResultIndex {
	private int count = -1;

	int getNext() {
	    count = count + 1;
	    return count;
	}
    }
}
