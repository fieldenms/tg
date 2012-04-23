package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;

final class EntityResultTreeBuilder {
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder() {
    }

    private class MetaP {
	String name;
	Class type;
	ICompositeUserTypeInstantiate hibType;
	SortedSet<PropertyPersistenceInfo> items;

	public MetaP(final String name, final Class type, final SortedSet<PropertyPersistenceInfo> items) {
	    super();
	    this.name = name;
	    this.type = type;
	    this.items = items;
	    this.hibType = null;
	}
	public MetaP(final String name, final ICompositeUserTypeInstantiate hibType, final SortedSet<PropertyPersistenceInfo> items) {
	    super();
	    this.name = name;
	    this.type = null;
	    this.items = items;
	    this.hibType = hibType;
	}

    }

    protected <E extends AbstractEntity<?>> EntityTree<E> buildEntityTree(final Class<E> resultType, final SortedSet<PropertyPersistenceInfo> properties) throws Exception {
	final EntityTree<E> result = new EntityTree<E>(resultType);

	final List<PropertyPersistenceInfo> singleProps = getFirstLevelSingleProps(resultType, properties);
	final Map<String, MetaP> compositeProps = getFirstLevelCompositeProps(resultType, properties);
	final Map<String, MetaP> compositeValueProps = getFirstLevelCompositeValueProps(resultType, properties);

	for (final PropertyPersistenceInfo propInfo : singleProps) {
	    result.getSingles().put(new PropertyPersistenceInfo.Builder(propInfo.getName(), propInfo.getJavaType(), propInfo.isNullable()). //
		    column(propInfo.getColumn()). //
		    hibType(propInfo.getHibType()). //
		    build(), index.getNext());
	}

	for (final Map.Entry<String, MetaP> propEntry : compositeValueProps.entrySet()) {
	    final String subtreePropName = propEntry.getKey();
	    result.getCompositeValues().put(subtreePropName, buildValueTree(propEntry.getValue().hibType, propEntry.getValue().items));
	}

	for (final Map.Entry<String, MetaP> propEntry : compositeProps.entrySet()) {
	    final String subtreePropName = propEntry.getKey();
	    result.getComposites().put(subtreePropName, buildEntityTree(propEntry.getValue().type, propEntry.getValue().items));
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
    private  <E extends AbstractEntity<?>> List<PropertyPersistenceInfo> getFirstLevelSingleProps(final Class<E> resultType, final Collection<PropertyPersistenceInfo> allProps) {
	final List<PropertyPersistenceInfo> result = new ArrayList<PropertyPersistenceInfo>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if (((prop.getJavaType() == null || !prop.isEntity()) && !prop.getName().contains(".")) & !prop.isCompositeProperty()) {
		result.add(prop);
	    }
	}

	return result;
    }

    private <E extends AbstractEntity<?>> Map<String, MetaP> getFirstLevelCompositeProps(final Class<E> resultType, final Collection<PropertyPersistenceInfo> allProps) {
	final Map<String, MetaP> result = new HashMap<String, MetaP>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);

		if (EntityAggregates.class != resultType) {
		    if (result.containsKey(group)) {
			result.get(group).items.add(new PropertyPersistenceInfo.Builder(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), false/*?*/). //
			column(prop.getColumn()). //
			hibType(prop.getHibType()). //
			build());

		    }
		} else {
		    if (!result.containsKey(group)) {
			result.put(group, new MetaP(group, EntityAggregates.class, new TreeSet<PropertyPersistenceInfo>()));
		    }
		    result.get(group).items.add(new PropertyPersistenceInfo.Builder(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), false/*?*/). //
		    column(prop.getColumn()). //
		    hibType(prop.getHibType()). //
		    build());

		}

	    } else if (prop.isEntity()) {
		final SortedSet<PropertyPersistenceInfo> subprops = new TreeSet<PropertyPersistenceInfo>();
		subprops.add(new PropertyPersistenceInfo.Builder("id", Long.class, false).column(prop.getColumn()).hibType(prop.getHibType()).build());
		result.put(prop.getName(), new MetaP(prop.getName(), prop.getJavaType(), subprops));
	    }
	}

	return result;
    }

    private <E extends AbstractEntity<?>> Map<String, MetaP> getFirstLevelCompositeValueProps(final Class<E> resultType, final SortedSet<PropertyPersistenceInfo> allProps) {
	final Map<String, MetaP> result = new HashMap<String, MetaP>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);

//		if (EntityAggregates.class != resultType) {
//		    final PropertyPersistenceInfo groupPpi = domainPersistenceMetadata.getPropPersistenceInfoExplicitly(resultType, group);

//		    if (groupPpi.isCompositeProperty()) {
			if (result.containsKey(group)) {
				result.get(group).items.add(new PropertyPersistenceInfo.Builder(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), false /*?*/). //
					column(prop.getColumn()). //
					hibType(prop.getHibType()). //
					build());
			}
//		    }
//		}
	    } else if (prop.isCompositeProperty()) {
		final SortedSet<PropertyPersistenceInfo> subprops = new TreeSet<PropertyPersistenceInfo>();
		result.put(prop.getName(), new MetaP(prop.getName(), prop.getHibTypeAsCompositeUserType(), subprops));
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