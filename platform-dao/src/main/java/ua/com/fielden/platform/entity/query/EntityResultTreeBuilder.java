package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;

final class EntityResultTreeBuilder {
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder() {
    }

    private class MetaP {
	String name;
	Class type;
	ICompositeUserTypeInstantiate hibType;
	SortedSet<ResultQueryYieldDetails> items;

	public MetaP(final String name, final Class type, final SortedSet<ResultQueryYieldDetails> items) {
	    super();
	    this.name = name;
	    this.type = type;
	    this.items = items;
	    this.hibType = null;
	}
	public MetaP(final String name, final ICompositeUserTypeInstantiate hibType, final SortedSet<ResultQueryYieldDetails> items) {
	    super();
	    this.name = name;
	    this.type = null;
	    this.items = items;
	    this.hibType = hibType;
	}

    }

    protected <E extends AbstractEntity<?>> EntityTree<E> buildEntityTree(final Class<E> resultType, final SortedSet<ResultQueryYieldDetails> properties) throws Exception {
	final EntityTree<E> result = new EntityTree<E>(resultType);

	final List<ResultQueryYieldDetails> singleProps = getFirstLevelSingleProps(resultType, properties);
	final Map<String, MetaP> compositeProps = getFirstLevelCompositeProps(resultType, properties);
	final Map<String, MetaP> compositeValueProps = getFirstLevelCompositeValueProps(resultType, properties);

	for (final ResultQueryYieldDetails propInfo : singleProps) {
	    result.getSingles().put(new ResultQueryYieldDetails(propInfo.getName(), propInfo.getJavaType(), propInfo.getHibType(), propInfo.getColumn()), index.getNext());
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

    protected ValueTree buildValueTree(final ICompositeUserTypeInstantiate hibType, final Collection<ResultQueryYieldDetails> properties) throws Exception {
	final ValueTree result = new ValueTree(hibType);

	final List<ResultQueryYieldDetails> singleProps = getFirstLevelSingleProps(null, properties);

	for (final ResultQueryYieldDetails propInfo : singleProps) {
	    result.getSingles().put(new ResultQueryYieldDetails(propInfo.getName(), propInfo.getJavaType(), propInfo.getHibType(), propInfo.getColumn()), index.getNext());
	}

	return result;
    }

    /**
     * Extract explicit and implicit property groups; Explicit property groups are provided with property type.
     *
     * @param allProps
     * @return
     */
    private  <E extends AbstractEntity<?>> List<ResultQueryYieldDetails> getFirstLevelSingleProps(final Class<E> resultType, final Collection<ResultQueryYieldDetails> allProps) {
	final List<ResultQueryYieldDetails> result = new ArrayList<ResultQueryYieldDetails>();

	for (final ResultQueryYieldDetails prop : allProps) {
	    if (((prop.getJavaType() == null || !prop.isEntity()) && !prop.getName().contains(".")) & !prop.isCompositeProperty()) {
		result.add(prop);
	    }
	}

	return result;
    }

    private <E extends AbstractEntity<?>> Map<String, MetaP> getFirstLevelCompositeProps(final Class<E> resultType, final Collection<ResultQueryYieldDetails> allProps) {
	final Map<String, MetaP> result = new HashMap<String, MetaP>();

	for (final ResultQueryYieldDetails prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);

		if (EntityAggregates.class != resultType) {
		    if (result.containsKey(group)) {
			result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn()));

		    }
		} else {
		    if (!result.containsKey(group)) {
			result.put(group, new MetaP(group, EntityAggregates.class, new TreeSet<ResultQueryYieldDetails>()));
		    }
		    result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn()));

		}

	    } else if (prop.isEntity()) {
		final SortedSet<ResultQueryYieldDetails> subprops = new TreeSet<ResultQueryYieldDetails>();
		subprops.add(new ResultQueryYieldDetails("id", Long.class, prop.getHibType(), prop.getColumn()));
		result.put(prop.getName(), new MetaP(prop.getName(), prop.getJavaType(), subprops));
	    }
	}

	return result;
    }

    private <E extends AbstractEntity<?>> Map<String, MetaP> getFirstLevelCompositeValueProps(final Class<E> resultType, final SortedSet<ResultQueryYieldDetails> allProps) {
	final Map<String, MetaP> result = new HashMap<String, MetaP>();

	for (final ResultQueryYieldDetails prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);

//		if (EntityAggregates.class != resultType) {
//		    final PropertyPersistenceInfo groupPpi = domainPersistenceMetadata.getPropPersistenceInfoExplicitly(resultType, group);

//		    if (groupPpi.isCompositeProperty()) {
			if (result.containsKey(group)) {
				result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn()));
			}
//		    }
//		}
	    } else if (prop.isCompositeProperty()) {
		final SortedSet<ResultQueryYieldDetails> subprops = new TreeSet<ResultQueryYieldDetails>();
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