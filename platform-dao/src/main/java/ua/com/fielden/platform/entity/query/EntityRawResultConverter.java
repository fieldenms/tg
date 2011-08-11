package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

public class EntityRawResultConverter<E extends AbstractEntity> {
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
    protected List<EntityContainer<E>> transformFromNativeResult(final EntityTree resultTree, final List<Object> nativeResult) {
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

    /*DONE*/
    private EntityContainer<E> transformTuple(final Object[] data, final EntityTree resultTree, final boolean shouldBeFetched) {

	final EntityContainer<E> entCont = new EntityContainer<E>(resultTree.getResultType(), shouldBeFetched);

	for (final Map.Entry<PropColumn, Integer> primEntry : resultTree.getSingles().entrySet()) {
	    entCont.primitives.put(primEntry.getKey().getName(), convertValue(data[(primEntry.getValue())], primEntry.getKey().getHibType(), primEntry.getKey().getHibUserType()));
	}

	for (final Map.Entry<String, EntityTree> entityEntry : resultTree.getComposites().entrySet()) {
	    entCont.entities.put(entityEntry.getKey(), transformTuple(data, entityEntry.getValue(), shouldBeFetched));
	}

	return entCont;
    }

    /*DONE*/
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
