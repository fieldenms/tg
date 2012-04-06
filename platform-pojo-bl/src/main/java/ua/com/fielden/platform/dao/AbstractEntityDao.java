package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IPlainJoin;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * Provides common implementation shared between Hibernate and REST implementation of DAOs.
 *
 * @author TG Team
 *
 */
public abstract class AbstractEntityDao<T extends AbstractEntity<?>> implements IEntityDao<T> {

    protected final static String ID_PROPERTY_NAME = "id";
    private final Class<? extends Comparable> keyType;
    private final Class<T> entityType;
    private final QueryExecutionModel<T, EntityResultQueryModel<T>> defaultModel;


    /**
     * A principle constructor, which requires entity type that should be managed by this DAO instance. Entity's key type is determined automatically.
     *
     * @param entityType
     */
    protected AbstractEntityDao() {
	final EntityType annotation = AnnotationReflector.getAnnotation(EntityType.class, getClass());
	if (annotation == null) {
	    throw new IllegalStateException("Controller " + getClass().getName() + " is missing EntityType annotation.");
	}
	this.entityType = (Class<T>) annotation.value();
	this.keyType = AnnotationReflector.getKeyType(entityType);
	this.defaultModel = produceDefaultQueryExecutionModel(entityType);
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> produceDefaultQueryExecutionModel(final Class<T> entityType) {
	final EntityResultQueryModel<T> query = select(entityType).model();
	final OrderingModel orderBy = orderBy().prop(ID_PROPERTY_NAME).asc().model();
	return from(query).with(orderBy).build();
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> getDefaultQueryExecutionModel() {
	return defaultModel;
    }

    @Override
    public Class<T> getEntityType() {
	return entityType;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
	return keyType;
    }

    @Override
    public T findById(final Long id, final fetch<T> fetchModel) {
	return fetchOneEntityInstance(id, fetchModel);
    }

    @Override
    public T findById(final Long id) {
	return fetchOneEntityInstance(id, null);
    }

    private T fetchOneEntityInstance(final Long id, final fetch<T> fetchModel) {
	try {
	    final EntityResultQueryModel<T> query = select(getEntityType()).where().prop(ID_PROPERTY_NAME).eq().val(id).model();
	    return getEntity(from(query).with(fetchModel).build());
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Method checks whether the key of the entity type associated with this DAO if composite or not.
     *
     * If composite then <code>WHERE</code> statement is build using composite key members and the passed values. The number of values should match the number of composite key
     * members.
     *
     * Otherwise, <code>WHERE</code> statement is build using only property <code>key</code>.
     *
     * The created query expects a unique result, and throws a runtime exception if this is not the case.
     *
     * TODO Need to consider the case of polymorphic associations such as Rotable, which can be both Bogie and/or Wheelset.
     */
    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
	final T entity = findByKeyAndFetch(null, keyValues);
	return entity != null;
    }

    @Override
    public T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues) {
	try {
	    return getEntity(from((createQueryByKey(keyValues))).with(fetchModel).build());
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    @Override
    public T findByKey(final Object... keyValues) {
	return findByKeyAndFetch(null, keyValues);
    }

    /**
     * Convenient method for composing a query to select an entity by key value.
     *
     * @param keyValues
     * @return
     */
    protected EntityResultQueryModel<T> createQueryByKey(final Object... keyValues) {
	final IPlainJoin qry = select(getEntityType());

	if (getKeyType() == DynamicEntityKey.class) {
	    final List<Field> list = Finder.getKeyMembers(getEntityType());
	    // let's be smart about the key values and support the case where an instance of DynamicEntityKey is passed.
	    final Object[] realKeyValues = (keyValues.length == 1 && keyValues[0].getClass() == DynamicEntityKey.class) ? //
	    ((DynamicEntityKey) keyValues[0]).getKeyValues()
		    : keyValues;

	    if (list.size() != realKeyValues.length) {
		throw new IllegalArgumentException("The number of provided values (" + realKeyValues.length
			+ ") does not match the number of properties in the entity composite key (" + list.size() + ").");
	    }

	    ICompoundCondition0 cc = qry//
	    .where().prop(list.get(0).getName())//
	    .eq().val(realKeyValues[0]);

	    for (int index = 1; index < list.size(); index++) {
		cc = cc.and().prop(list.get(index).getName()).eq().val(realKeyValues[index]); // all conditions are linked with AND by default
	    }
	    return cc.model();
	} else if (keyValues.length != 1) {
	    throw new IllegalArgumentException("Only one key value is expected instead of " + keyValues.length + " when looking for an entity by a non-composite key.");
	} else {
	    return qry//
		    .where().prop("key")//
		    .eq().val(keyValues[0]).model();
	}
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> summaryModel, final int pageCapacity) {
	throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void delete(final T entity) {
	throw new UnsupportedOperationException("By default deletion is not supported.");
    }

    @Override
    public void delete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
	throw new UnsupportedOperationException("By default deletion is not supported.");
    }

    @Override
    public void delete(final EntityResultQueryModel<T> model) {
	delete(model, Collections.<String, Object> emptyMap());
    }
}