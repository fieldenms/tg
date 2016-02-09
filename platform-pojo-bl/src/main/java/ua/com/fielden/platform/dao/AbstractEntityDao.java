package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IPlainJoin;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Provides common implementation shared between Hibernate and REST implementation of DAOs.
 *
 * @author TG Team
 *
 */
public abstract class AbstractEntityDao<T extends AbstractEntity<?>> implements IEntityDao<T> {

    private final Class<? extends Comparable> keyType;
    private final Class<T> entityType;
    private IFetchProvider<T> fetchProvider;

    protected boolean getFilterable() {
        return false;
    }

    /**
     * A principle constructor, which requires entity type that should be managed by this DAO instance. Entity's key type is determined automatically.
     *
     * @param entityType
     */
    protected AbstractEntityDao() {
        final EntityType annotation = AnnotationReflector.getAnnotation(getClass(), EntityType.class);
        if (annotation == null) {
            throw new IllegalStateException("Controller " + getClass().getName() + " is missing EntityType annotation.");
        }
        this.entityType = (Class<T>) annotation.value();
        this.keyType = AnnotationReflector.getKeyType(entityType);
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> produceDefaultQueryExecutionModel(final Class<T> entityType) {
        final EntityResultQueryModel<T> query = select(entityType).model();
        query.setFilterable(getFilterable());
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.ID).asc().model();
        return from(query).with(orderBy).model();
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> getDefaultQueryExecutionModel() {
        return produceDefaultQueryExecutionModel(entityType);
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
            final EntityResultQueryModel<T> query = select(getEntityType()).where().prop(AbstractEntity.ID).eq().val(id).model();
            query.setFilterable(getFilterable());
            return getEntity(from(query).with(fetchModel).model());
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
            return getEntity(from((createQueryByKey(keyValues))).with(fetchModel).model());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public T findByKey(final Object... keyValues) {
        return findByKeyAndFetch(null, keyValues);
    }

    @Override
    public T findByEntityAndFetch(final fetch<T> fetchModel, final T entity) {
        if (entity.getId() != null) {
            return findById(entity.getId(), fetchModel);
        } else {
            return findByKeyAndFetch(fetchModel, entity.getKey());
        }
    }

    /**
     * Convenient method for composing a query to select an entity by key value.
     *
     * @param keyValues
     * @return
     */
    protected EntityResultQueryModel<T> createQueryByKey(final Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            throw new IllegalArgumentException("No key values provided.");
        }

        final IPlainJoin<T> qry = select(getEntityType());

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

            ICompoundCondition0<T> cc = qry.where().condition(buildConditionForKeyMember(list.get(0).getName(), list.get(0).getType(), realKeyValues[0]));

            for (int index = 1; index < list.size(); index++) {
                cc = cc.and().condition(buildConditionForKeyMember(list.get(index).getName(), list.get(index).getType(), realKeyValues[index]));
            }
            final EntityResultQueryModel<T> query = cc.model();
            query.setFilterable(getFilterable());
            return query;
        } else if (keyValues.length != 1) {
            throw new IllegalArgumentException("Only one key value is expected instead of " + keyValues.length + " when looking for an entity by a non-composite key.");
        } else {
            final EntityResultQueryModel<T> query = qry.where().condition(buildConditionForKeyMember(AbstractEntity.KEY, getKeyType(), keyValues[0])).model();
            query.setFilterable(getFilterable());
            return query;
        }
    }

    private ConditionModel buildConditionForKeyMember(final String propName, final Class propType, final Object propValue) {
        if (propValue == null) {
            return cond().prop(propName).isNull().model();
        } else if (String.class.equals(propType)) {
            return cond().lowerCase().prop(propName).eq().lowerCase().val(propValue).model();
        } else {
            return cond().prop(propName).eq().val(propValue).model();
        }
    }

    @Override
    public final IFetchProvider<T> getFetchProvider() {
        if (fetchProvider == null) {
            fetchProvider = createFetchProvider();
        }
        return fetchProvider;
    }

    /**
     * Creates fetch provider for this entity companion.
     * <p>
     * Should be overridden to provide custom fetch provider.
     *
     * @return
     */
    protected IFetchProvider<T> createFetchProvider() {
        // provides a very minimalistic version of fetch provider by default (only id and version are included)
        return EntityUtils.fetch(getEntityType());
    }
}