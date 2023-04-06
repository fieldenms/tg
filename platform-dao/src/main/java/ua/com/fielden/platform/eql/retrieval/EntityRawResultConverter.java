package ua.com.fielden.platform.eql.retrieval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.ValueContainer;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.QueryResultLeaf;
import ua.com.fielden.platform.eql.retrieval.records.ValueTree;

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

        final EntityContainer<ET> entCont = new EntityContainer<>(resultTree.resultType());

        for (final QueryResultLeaf leaf : resultTree.leaves()) {
            entCont.getPrimitives().put(leaf.name(), convertValue(data[(leaf.position())], leaf.hibUserType()));
        }

        for (final Map.Entry<String, ValueTree> valueTreeEntry : resultTree.valueTrees().entrySet()) {
            entCont.getComposites().put(valueTreeEntry.getKey(), transformTupleIntoValueContainer(data, valueTreeEntry.getValue()));
        }

        for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> entityTreeEntry : resultTree.entityTrees().entrySet()) {
            final EntityContainer<? extends AbstractEntity<?>> entContainer = transformTupleIntoEntityContainer(data, entityTreeEntry.getValue());
            entCont.getEntities().put(entityTreeEntry.getKey(), entContainer);
        }

        return entCont;
    }

    private ValueContainer transformTupleIntoValueContainer(final Object[] data, final ValueTree valueTree) {
        final Map<String, Object> converted = new HashMap<String, Object>();
        for (final QueryResultLeaf leaf : valueTree.leaves()) {
            converted.put(leaf.name(), convertValue(data[(leaf.position())], leaf.hibUserType()));
        }

        return new ValueContainer(valueTree.hibCompositeType(), converted);
    }

    private Object convertValue(final Object rawValue, final IUserTypeInstantiate userType) {
        return userType == null ? rawValue : userType.instantiate(rawValue, entityFactory);
    }
}