package ua.com.fielden.platform.eql.retrieval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.ValueContainer;

public class EntityRawResultConverter<E extends AbstractEntity<?>> {
    private final EntityFactory entityFactory;

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
    protected List<EntityContainer<E>> transformFromNativeResult(final EntityTree<E> resultTree, final List<?> nativeResult) {
        final List<EntityContainer<E>> result = new ArrayList<>();

        for (final Object nativeEntry : nativeResult) {
            final Object[] nativeEntries = nativeEntry instanceof Object[] ? (Object[]) nativeEntry : new Object[] { nativeEntry };
            result.add(transformTupleIntoEntityContainer(nativeEntries, resultTree));
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
    private <ET extends AbstractEntity<?>> EntityContainer<ET> transformTupleIntoEntityContainer(final Object[] data, final EntityTree<ET> resultTree) {

        final EntityContainer<ET> entCont = new EntityContainer<>(resultTree.resultType);

        for (final Entry<Integer, YieldDetails> primEntry : resultTree.getSingles().entrySet()) {
            entCont.getPrimitives().put(primEntry.getValue().name, convertValue(data[(primEntry.getKey())], primEntry.getValue().getHibTypeAsUserType()));
        }

        for (final Map.Entry<String, ValueTree> compositeEntry : resultTree.getCompositeValues().entrySet()) {
            entCont.getComposites().put(compositeEntry.getKey(), transformTuple(data, compositeEntry.getValue()));
        }

        for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> entityEntry : resultTree.getComposites().entrySet()) {
            final EntityContainer<? extends AbstractEntity<?>> entContainer = transformTupleIntoEntityContainer(data, entityEntry.getValue());
            entCont.getEntities().put(entityEntry.getKey(), entContainer);
        }

        return entCont;
    }

    private Map<String, Object> convertValuesForPrimitives (final Object[] data, final ValueTree resultTree) {
        final Map<String, Object> primitives = new HashMap<String, Object>();
        for (final Entry<Integer, YieldDetails> primEntry : resultTree.getSingles().entrySet()) {
            primitives.put(primEntry.getValue().name, convertValue(data[(primEntry.getKey())], primEntry.getValue().getHibTypeAsUserType()));
        }
        return primitives;
    }
    
    private ValueContainer transformTuple(final Object[] data, final ValueTree resultTree) {
        return new ValueContainer(resultTree.hibType, convertValuesForPrimitives(data, resultTree));
    }

    private Object convertValue(final Object rawValue, final IUserTypeInstantiate userType) {
        return userType == null ? rawValue : userType.instantiate(rawValue, entityFactory);
    }
}