package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;

final class EntityResultTreeBuilder {
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder() {
    }

    private class MetaP {
	ResultQueryYieldDetails parent;
	SortedSet<ResultQueryYieldDetails> items = new TreeSet<ResultQueryYieldDetails>();
	MetaPType metaPType;

	public MetaP(final ResultQueryYieldDetails parent, final MetaPType metaPType) {
	    this.parent = parent;
	    this.metaPType = metaPType;
	}
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
		    if (resultType == EntityAggregates.class) {
			result.put(group, new MetaP(new ResultQueryYieldDetails(group, EntityAggregates.class, null, null, YieldDetailsType.USUAL_PROP), MetaPType.ENTITY_PROP));
		    } else {
			if (Money.class.equals(PropertyTypeDeterminator.determinePropertyType(resultType, group))) {
			    result.put(group, new MetaP(new ResultQueryYieldDetails(group, Money.class, new SimpleMoneyType(), null, YieldDetailsType.COMPOSITE_TYPE_HEADER), MetaPType.COMPOSITE_TYPE_PROP));
			} else {
			    throw new IllegalStateException("Not implemented yet: " + prop.getName());
			}
		    }
		}
		result.get(group).items.add(new ResultQueryYieldDetails(prop.getName().substring(firstDotIndex + 1), prop.getJavaType(), prop.getHibType(), prop.getColumn(), prop.getYieldDetailsType()));
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

	for (final MetaP propEntry : compositeProps.values()) {
	    if (propEntry.metaPType.equals(MetaPType.USUAL_PROP)) {
		result.getSingles().put(new ResultQueryYieldDetails(propEntry.parent.getName(), propEntry.parent.getJavaType(), propEntry.parent.getHibType(), propEntry.parent.getColumn(), propEntry.parent.getYieldDetailsType()), index.getNext());
	    } else if (propEntry.metaPType.equals(MetaPType.ENTITY_PROP)) {
		result.getComposites().put(propEntry.parent.getName(), buildEntityTree(propEntry.parent.getJavaType(), propEntry.items));
	    } else if (propEntry.metaPType.equals(MetaPType.COMPOSITE_TYPE_PROP)) {
		result.getCompositeValues().put(propEntry.parent.getName(), buildValueTree(propEntry.parent.getHibTypeAsCompositeUserType(), propEntry.items));
	    } else if (propEntry.metaPType.equals(MetaPType.UNION_ENTITY_PROP)) {
		result.getComposites().put(propEntry.parent.getName(), buildEntityTree(propEntry.parent.getJavaType(), propEntry.items));
	    }
	}

	return result;
    }

    protected ValueTree buildValueTree(final ICompositeUserTypeInstantiate hibType, final SortedSet<ResultQueryYieldDetails> properties) throws Exception {
	final ValueTree result = new ValueTree(hibType);

	final Map<String, MetaP> compositeProps = getPropsHierarchy(null, properties);

	for (final MetaP propEntry : compositeProps.values()) {
	    if (propEntry.metaPType.equals(MetaPType.USUAL_PROP)) {
		result.getSingles().put(new ResultQueryYieldDetails(propEntry.parent.getName(), propEntry.parent.getJavaType(), propEntry.parent.getHibType(), propEntry.parent.getColumn(), propEntry.parent.getYieldDetailsType()), index.getNext());
	    } else {
		throw new IllegalStateException("Unexpected props while constructing ValueTree!");
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