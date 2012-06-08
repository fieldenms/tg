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
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType;

final class EntityResultTreeBuilder {
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder() {
    }

    private class MetaP {
//	String name;
//	Class type;
//	ICompositeUserTypeInstantiate hibType;
	ResultQueryYieldDetails parent;
	SortedSet<ResultQueryYieldDetails> items = new TreeSet<ResultQueryYieldDetails>();
	MetaPType metaPType;

	public MetaP(final ResultQueryYieldDetails parent, final MetaPType metaPType) {
	    this.parent = parent;
	    this.metaPType = metaPType;
	}
//
//
//	private MetaP(final String name, final MetaPType metaPType, final Class type, final ICompositeUserTypeInstantiate hibType) {
//	    super();
//	    this.name = name;
//	    this.type = type;
//	    this.hibType = hibType;
//	    this.metaPType = metaPType;
//	    this.type = type;
//	}
//
//	public MetaP(final String name, final MetaPType metaPType, final Class type) {
//	    this(name, metaPType, type, null);
//	}
//
//	public MetaP(final String name, final MetaPType metaPType, final ICompositeUserTypeInstantiate hibType) {
//	    this(name, metaPType, null, hibType);
//	}
    }

    public static enum MetaPType {
	USUAL_PROP, //
	ENTITY_PROP,
	UNION_ENTITY_PROP, //
	COMPOSITE_TYPE_PROP;
    }

    protected <E extends AbstractEntity<?>> Map<String, MetaP> getPropsHierarchy(final Class<E> resultType, final SortedSet<ResultQueryYieldDetails> properties) throws Exception {
	final Map<String, MetaP> result = new HashMap<String, MetaP>();

	for (final ResultQueryYieldDetails prop : properties) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);
		    if (!result.containsKey(group)) {
			result.put(group, new MetaP(new ResultQueryYieldDetails(group, EntityAggregates.class, null, null, YieldDetailsType.USUAL_PROP), MetaPType.ENTITY_PROP));
		    }

		if (result.containsKey(group)) {
		    result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn(), prop.getYieldDetailsType()));
		} else {
		    throw new IllegalStateException("CAN'T BE!");
		    //case of entity aggregates
		}
	    } else if (prop.isCompositeProperty()) {
		result.put(prop.getName(), new MetaP(prop, MetaPType.COMPOSITE_TYPE_PROP));
	    } else if (prop.isEntity()) {
		final ResultQueryYieldDetails idProp = new ResultQueryYieldDetails(AbstractEntity.ID, Long.class, prop.getHibType(), prop.getColumn(), YieldDetailsType.USUAL_PROP);
		final MetaP entityMetaP = new MetaP(prop, MetaPType.ENTITY_PROP);
		entityMetaP.items.add(idProp);
		result.put(prop.getName(), entityMetaP);
	    } else if (prop.isUnionEntity()) {
		result.put(prop.getName(), new MetaP(prop, MetaPType.UNION_ENTITY_PROP));
	    } else {
		result.put(prop.getName(), new MetaP(prop, MetaPType.USUAL_PROP));
	    }
	}

	return result;
    }

    protected <E extends AbstractEntity<?>> EntityTree<E> buildEntityTree(final Class<E> resultType, final SortedSet<ResultQueryYieldDetails> properties) throws Exception {
	final EntityTree<E> result = new EntityTree<E>(resultType);

	final Map<String, MetaP> compositeProps = getPropsHierarchy(resultType, properties);

//	for (final ResultQueryYieldDetails propInfo : singleProps) {
//	    result.getSingles().put(new ResultQueryYieldDetails(propInfo.getName(), propInfo.getJavaType(), propInfo.getHibType(), propInfo.getColumn(), propInfo.getYieldDetailsType()), index.getNext());
//	}
//
//	for (final Map.Entry<String, MetaP> propEntry : compositeValueProps.entrySet()) {
//	    final String subtreePropName = propEntry.getKey();
//	    result.getCompositeValues().put(subtreePropName, buildValueTree(propEntry.getValue().hibType, propEntry.getValue().items));
//	}

	for (final MetaP propEntry : compositeProps.values()) {
	    if (propEntry.metaPType.equals(MetaPType.USUAL_PROP)) {
		result.getSingles().put(new ResultQueryYieldDetails(propEntry.parent.getName(), propEntry.parent.getJavaType(), propEntry.parent.getHibType(), propEntry.parent.getColumn(), propEntry.parent.getYieldDetailsType()), index.getNext());
	    } else if (propEntry.metaPType.equals(MetaPType.ENTITY_PROP)) {
		result.getComposites().put(propEntry.parent.getName(), buildEntityTree(propEntry.parent.getJavaType(), propEntry.items));
	    } else if (propEntry.metaPType.equals(MetaPType.COMPOSITE_TYPE_PROP)) {
		result.getCompositeValues().put(propEntry.parent.getName(), buildValueTree(propEntry.parent.getHibTypeAsCompositeUserType(), propEntry.items));
	    }
	    else if (propEntry.metaPType.equals(MetaPType.UNION_ENTITY_PROP)) {
		System.out.println(propEntry.parent.getJavaType());
		System.out.println(propEntry.items);
		result.getComposites().put(propEntry.parent.getName(), buildEntityTree(propEntry.parent.getJavaType(), propEntry.items));
	    }
	}

	return result;
    }


//    protected <E extends AbstractEntity<?>> EntityTree<E> buildEntityTree(final Class<E> resultType, final SortedSet<ResultQueryYieldDetails> properties) throws Exception {
//	final EntityTree<E> result = new EntityTree<E>(resultType);
//
//	final List<ResultQueryYieldDetails> singleProps = getFirstLevelSingleProps(resultType, properties);
//	final Map<String, MetaP> compositeProps = getFirstLevelCompositeProps(resultType, properties);
//	final Map<String, MetaP> compositeValueProps = getFirstLevelCompositeValueProps(resultType, properties);
//
//	for (final ResultQueryYieldDetails propInfo : singleProps) {
//	    result.getSingles().put(new ResultQueryYieldDetails(propInfo.getName(), propInfo.getJavaType(), propInfo.getHibType(), propInfo.getColumn(), propInfo.getYieldDetailsType()), index.getNext());
//	}
//
//	for (final Map.Entry<String, MetaP> propEntry : compositeValueProps.entrySet()) {
//	    final String subtreePropName = propEntry.getKey();
//	    result.getCompositeValues().put(subtreePropName, buildValueTree(propEntry.getValue().hibType, propEntry.getValue().items));
//	}
//
//	for (final Map.Entry<String, MetaP> propEntry : compositeProps.entrySet()) {
//	    final String subtreePropName = propEntry.getKey();
//	    result.getComposites().put(subtreePropName, buildEntityTree(propEntry.getValue().type, propEntry.getValue().items));
//	}
//
//	return result;
//    }

    protected ValueTree buildValueTree(final ICompositeUserTypeInstantiate hibType, final Collection<ResultQueryYieldDetails> properties) throws Exception {
	final ValueTree result = new ValueTree(hibType);

	final List<ResultQueryYieldDetails> singleProps = getFirstLevelSingleProps(null, properties);

	for (final ResultQueryYieldDetails propInfo : singleProps) {
	    result.getSingles().put(new ResultQueryYieldDetails(propInfo.getName(), propInfo.getJavaType(), propInfo.getHibType(), propInfo.getColumn(), propInfo.getYieldDetailsType()), index.getNext());
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
//	    if ((prop.getJavaType() == null || !prop.isEntity()) && prop.getYieldDetailsType().equals(YieldDetailsType.USUAL_PROP)) { //!prop.getName().contains(".")) & !prop.isCompositeProperty()) {
	    if (((prop.getJavaType() == null || !prop.isEntity()) && !prop.getName().contains(".")) & !prop.isCompositeProperty()) {
		result.add(prop);
	    }
	}

	return result;
    }

//    private <E extends AbstractEntity<?>> Map<String, MetaP> getFirstLevelCompositeProps(final Class<E> resultType, final Collection<ResultQueryYieldDetails> allProps) {
//	final Map<String, MetaP> result = new HashMap<String, MetaP>();
//
//	for (final ResultQueryYieldDetails prop : allProps) {
//	    if (prop.getName().contains(".")) {
//		final int firstDotIndex = prop.getName().indexOf(".");
//		final String group = prop.getName().substring(0, firstDotIndex);
//
//		if (EntityAggregates.class != resultType) {
//		    if (result.containsKey(group)) {
//			result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn(), prop.getYieldDetailsType()));
//
//		    }
//		} else {
//		    if (!result.containsKey(group)) {
//			result.put(group, new MetaP(group, MetaPType.ENTITY_PROP, EntityAggregates.class));
//		    }
//		    result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn(), prop.getYieldDetailsType()));
//
//		}
//
//	    } else if (prop.isEntity()) {
//		final ResultQueryYieldDetails idProp = new ResultQueryYieldDetails(AbstractEntity.ID, Long.class, prop.getHibType(), prop.getColumn(), YieldDetailsType.USUAL_PROP);
//		final MetaP entityMetaP = new MetaP(prop.getName(), MetaPType.ENTITY_PROP, prop.getJavaType());
//		entityMetaP.items.add(idProp);
//		result.put(prop.getName(), entityMetaP);
//	    }
//	}
//
//	return result;
//    }
//
//    private <E extends AbstractEntity<?>> Map<String, MetaP> getFirstLevelCompositeValueProps(final Class<E> resultType, final SortedSet<ResultQueryYieldDetails> allProps) {
//	final Map<String, MetaP> result = new HashMap<String, MetaP>();
//
//	for (final ResultQueryYieldDetails prop : allProps) {
//	    if (prop.getName().contains(".")) {
//		final int firstDotIndex = prop.getName().indexOf(".");
//		final String group = prop.getName().substring(0, firstDotIndex);
//
//		if (result.containsKey(group)) {
//		    result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn(), prop.getYieldDetailsType()));
//		}
//	    } else if (prop.isCompositeProperty()) {
//		result.put(prop.getName(), new MetaP(prop.getName(), MetaPType.COMPOSITE_TYPE_PROP, prop.getHibTypeAsCompositeUserType()));
//	    }
//	}
//
//	return result;
//    }

    private static class ResultIndex {
	private int count = -1;

	int getNext() {
	    count = count + 1;
	    return count;
	}
    }
}