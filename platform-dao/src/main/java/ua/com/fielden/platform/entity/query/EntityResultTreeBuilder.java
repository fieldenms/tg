package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;

final class EntityResultTreeBuilder {
    private MappingsGenerator mappingsGenerator;
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder(final MappingsGenerator mappingsGenerator) {
	this.mappingsGenerator = mappingsGenerator;
    }

    protected EntityTree buildEntityTree(final Class resultType, final Collection<PropertyPersistenceInfo> properties) throws Exception {
	final EntityTree result = new EntityTree(resultType);

	final List<PropertyPersistenceInfo> singleProps = getFirstLevelSingleProps(resultType, properties);
	final Map<String, Collection<PropertyPersistenceInfo>> compositeProps = getFirstLevelCompositeProps(resultType, properties);
	final Map<String, Collection<PropertyPersistenceInfo>> compositeValueProps = getFirstLevelCompositeValueProps(resultType, properties);

	for (final PropertyPersistenceInfo propInfo : singleProps) {
	    result.getSingles().put(new PropertyPersistenceInfo.Builder(propInfo.getName(), propInfo.getJavaType(), propInfo.isNullable()). //
		    column(propInfo.getColumn()). //
		    hibType(propInfo.getHibType()). //
		    build(), index.getNext());
	}

	for (final Map.Entry<String, Collection<PropertyPersistenceInfo>> propEntry : compositeValueProps.entrySet()) {
	    final String subtreePropName = propEntry.getKey();
	    final PropertyPersistenceInfo ppi = mappingsGenerator.getPropPersistenceInfoExplicitly(resultType, subtreePropName);
	    result.getCompositeValues().put(subtreePropName, buildValueTree(ppi.getHibTypeAsCompositeUserType(), propEntry.getValue()));
	}

	for (final Map.Entry<String, Collection<PropertyPersistenceInfo>> propEntry : compositeProps.entrySet()) {
	    final String subtreePropName = propEntry.getKey();
	    final PropertyPersistenceInfo ppi = mappingsGenerator.getPropPersistenceInfoExplicitly(resultType, subtreePropName);
	    result.getComposites().put(subtreePropName, buildEntityTree(ppi.getJavaType(), propEntry.getValue()));
	}

	return result;
    }

    protected ValueTree buildValueTree(final ICompositeUserTypeInstantiate hibType, final Collection<PropertyPersistenceInfo> properties) throws Exception {
	final ValueTree result = new ValueTree(hibType);

	final List<PropertyPersistenceInfo> singleProps = getFirstLevelSingleProps(null, properties);

	for (final PropertyPersistenceInfo propInfo : singleProps) {
	    result.getSingles().put(new PropertyPersistenceInfo.Builder(propInfo.getName(), propInfo.getJavaType(), propInfo.isNullable()). //
		    column(propInfo.getColumn()). //
		    hibType(propInfo.getHibType()). //
		    build(), index.getNext());
	}

	return result;
    }

    /**
     * Extract explicit and implicit property groups; Explicit property groups are provided with property type.
     *
     * @param allProps
     * @return
     */
    private List<PropertyPersistenceInfo> getFirstLevelSingleProps(final Class resultType, final Collection<PropertyPersistenceInfo> allProps) {
	final List<PropertyPersistenceInfo> result = new ArrayList<PropertyPersistenceInfo>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if ((prop.getJavaType() == null || !prop.isEntity()) && !prop.getName().contains(".")) {
		result.add(prop);
	    }
	}

	return result;
    }

    private Map<String, Collection<PropertyPersistenceInfo>> getFirstLevelCompositeProps(final Class resultType, final Collection<PropertyPersistenceInfo> allProps) {
	final Map<String, Collection<PropertyPersistenceInfo>> result = new HashMap<String, Collection<PropertyPersistenceInfo>>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);

		final PropertyPersistenceInfo groupPpi = mappingsGenerator.getPropPersistenceInfoExplicitly(resultType, group);

		if (groupPpi.isEntity()) {
			if (!result.containsKey(group)) {
			    result.put(group, new ArrayList<PropertyPersistenceInfo>());
			}
			result.get(group).add(new PropertyPersistenceInfo.Builder(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), false/*?*/). //
				column(prop.getColumn()). //
				hibType(prop.getHibType()). //
				build());

		}

	    } else if (prop.isEntity()) { //prop.getType() != null && EntityUtils.isPersistedEntityType(prop.getJavaType())) {
		final List<PropertyPersistenceInfo> subprops = new ArrayList<PropertyPersistenceInfo>();
		subprops.add(new PropertyPersistenceInfo.Builder("id", Long.class, false). //
			column(prop.getColumn()). //
			hibType(prop.getHibType()). //
			build());
		result.put(prop.getName(), subprops);
	    }
	}

	return result;
    }

    private Map<String, Collection<PropertyPersistenceInfo>> getFirstLevelCompositeValueProps(final Class resultType, final Collection<PropertyPersistenceInfo> allProps) {
	final Map<String, Collection<PropertyPersistenceInfo>> result = new HashMap<String, Collection<PropertyPersistenceInfo>>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);
		final PropertyPersistenceInfo groupPpi = mappingsGenerator.getPropPersistenceInfoExplicitly(resultType, group);

		if (groupPpi.isCompositeProperty()) {
			if (!result.containsKey(group)) {
			    result.put(group, new ArrayList<PropertyPersistenceInfo>());
			}
			result.get(group).add(new PropertyPersistenceInfo.Builder(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), false /*?*/). //
				column(prop.getColumn()). //
				hibType(prop.getHibType()). //
				build());
		}
	    }
	}

	return result;
    }

    private static class ResultIndex {
	private int count = -1;

	int getNext() {
	    count = count + 1;
	    return count;
	}
    }
}