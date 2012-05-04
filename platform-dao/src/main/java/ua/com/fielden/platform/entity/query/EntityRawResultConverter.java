package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;

public class EntityRawResultConverter<E extends AbstractEntity<?>> {
    private EntityFactory entityFactory;

    protected EntityRawResultConverter(final EntityFactory entityFactory) {
	this.entityFactory = entityFactory;
    }

    /**
     * Populates raw data in entity containers
     *
     * @param resultTree
     * @param nativeResult
     * @return
     */
    protected List<EntityContainer<E>> transformFromNativeResult(final EntityTree<E> resultTree, final List<Object> nativeResult) {
	final List<EntityContainer<E>> result = new ArrayList<EntityContainer<E>>();

	for (final Object nativeEntry : nativeResult) {
	    final Object[] nativeEntries = nativeEntry instanceof Object[] ? (Object[]) nativeEntry : new Object[] { nativeEntry };
	    result.add(transformTuple(nativeEntries, resultTree, true));
	}

	return result;
    }

    /**
     * Recursively populates entity container with raw data for one entity instance.
     *
     * @param data
     * @param resultTree
     * @param shouldBeFetched
     * @return
     */
    private <ET extends AbstractEntity<?>> EntityContainer<ET> transformTuple(final Object[] data, final EntityTree<ET> resultTree, final boolean shouldBeFetched) {

	final EntityContainer<ET> entCont = new EntityContainer<ET>(resultTree.getResultType(), shouldBeFetched);

	for (final Map.Entry<ResultQueryYieldDetails, Integer> primEntry : resultTree.getSingles().entrySet()) {
	    entCont.getPrimitives().put(primEntry.getKey().getName(), convertValue(data[(primEntry.getValue())], primEntry.getKey().getHibTypeAsType(), primEntry.getKey().getHibTypeAsUserType()));
	}

	for (final Map.Entry<String, ValueTree> compositeEntry : resultTree.getCompositeValues().entrySet()) {
	    entCont.getComposites().put(compositeEntry.getKey(), transformTuple(data, compositeEntry.getValue(), shouldBeFetched));
	}

	for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> entityEntry : resultTree.getComposites().entrySet()) {
	    final EntityContainer<? extends AbstractEntity<?>> entContainer = transformTuple(data, entityEntry.getValue(), shouldBeFetched);
	    if (entContainer != null || (entContainer == null && EntityAggregates.class.equals(resultTree.getResultType()))) {
		entCont.getEntities().put(entityEntry.getKey(), entContainer);
	    }
	}

	return !entCont.isEmpty() ? entCont : null;
    }

    private ValueContainer transformTuple(final Object[] data, final ValueTree resultTree, final boolean shouldBeFetched) {

	final ValueContainer entCont = new ValueContainer(resultTree.getHibType());

	for (final Map.Entry<ResultQueryYieldDetails, Integer> primEntry : resultTree.getSingles().entrySet()) {
	    entCont.primitives.put(primEntry.getKey().getName(), convertValue(data[(primEntry.getValue())], primEntry.getKey().getHibTypeAsType(), primEntry.getKey().getHibTypeAsUserType()));
	}

	return entCont;
    }

    private Object convertValue(final Object rawValue, final Type hibType, final IUserTypeInstantiate userType) {
	if (hibType != null) {
	    return rawValue;
	} else if (userType != null) {
	    return userType.instantiate(rawValue, entityFactory);
	} else {
	    return rawValue;
	}
    }
}