package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Provides common implementation shared between Hibernate and REST implementation of DAOs.
 *
 * @author TG Team
 *
 */
public abstract class AbstractEntityDao<T extends AbstractEntity<?>> implements IEntityDao<T> {

    private final Class<? extends Comparable<?>> keyType;
    private final Class<T> entityType;
    private IFetchProvider<T> fetchProvider;
    
    @Inject
    private EntityFactory entityFactory;

    protected boolean getFilterable() {
        return false;
    }

    /**
     * The default constructor, which looks for annotation {@link EntityType} to identify the entity type automatically.
     * An exception is thrown if the annotation is missing. 
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
        return instrumented() ? from(query).with(orderBy).model() : from(query).with(orderBy).lightweight().model();
    }

    protected QueryExecutionModel<T, EntityResultQueryModel<T>> getDefaultQueryExecutionModel() {
        return produceDefaultQueryExecutionModel(entityType);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
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
            return getEntity(instrumented() ? from(query).with(fetchModel).model(): from(query).with(fetchModel).lightweight().model());
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
            return getEntity(instrumented() ? from((createQueryByKey(keyValues))).with(fetchModel).model() : from((createQueryByKey(keyValues))).with(fetchModel).lightweight().model());
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
        final EntityResultQueryModel<T> query = attachKeyConditions(select(getEntityType()).where(), keyValues).model();
        query.setFilterable(getFilterable());
        return query;
    }
    
    /**
     * Creates a query for entities by their keys (simple or composite). If <code>entitiesWithKeys</code> are empty -- returns empty optional. 
     * 
     * @param entityType -- the entity type
     * @param entitiesWithKeys -- the entities with <b>all</b> key values correctly fetched / assigned
     * @return
     */
    public Optional<EntityResultQueryModel<T>> createQueryByKeyFor(final Collection<T> entitiesWithKeys) {
        IWhere0<T> partQ = select(getEntityType()).where();
        final List<Field> keyMembers = Finder.getKeyMembers(getEntityType());
        
        for (final Iterator<T> iter = entitiesWithKeys.iterator(); iter.hasNext();) {
            final T entityWithKey = iter.next();
            final ICompoundCondition0<T> or = attachKeyConditions(partQ, keyMembers, keyMembers.stream().map(keyMember -> entityWithKey.get(keyMember.getName())).toArray());
            if (iter.hasNext()) {
                partQ = or.or();
            } else {
                return Optional.of(or.model());
            }
        }

        return Optional.empty();
    }
    
    /**
     * Attaches key member conditions to the partially constructed query <code>entryPoint</code> based on its values. 
     * 
     * @param entryPoint
     * @param keyValues
     * @return
     */
    protected ICompoundCondition0<T> attachKeyConditions(final IWhere0<T> entryPoint, final Object... keyValues) {
        return attachKeyConditions(entryPoint, Finder.getKeyMembers(getEntityType()), keyValues);
    }

    /**
     * Attaches key member conditions to the partially constructed query <code>entryPoint</code> based on its values. 
     * 
     * @param entryPoint
     * @param keyMembers
     * @param keyValues
     * @return
     */
    protected ICompoundCondition0<T> attachKeyConditions(final IWhere0<T> entryPoint, final List<Field> keyMembers, final Object... keyValues) {
        if (getKeyType() == DynamicEntityKey.class) {
            // let's be smart about the key values and support the case where an instance of DynamicEntityKey is passed.
            final Object[] realKeyValues = (keyValues.length == 1 && keyValues[0].getClass() == DynamicEntityKey.class) ? //
            ((DynamicEntityKey) keyValues[0]).getKeyValues()
                    : keyValues;

            if (keyMembers.size() != realKeyValues.length) {
                throw new IllegalArgumentException("The number of provided values (" + realKeyValues.length
                        + ") does not match the number of properties in the entity composite key (" + keyMembers.size() + ").");
            }

            ICompoundCondition0<T> cc = entryPoint.condition(buildConditionForKeyMember(keyMembers.get(0).getName(), keyMembers.get(0).getType(), realKeyValues[0]));

            for (int index = 1; index < keyMembers.size(); index++) {
                cc = cc.and().condition(buildConditionForKeyMember(keyMembers.get(index).getName(), keyMembers.get(index).getType(), realKeyValues[index]));
            }
            return cc;
        } else if (keyValues.length != 1) {
            throw new IllegalArgumentException("Only one key value is expected instead of " + keyValues.length + " when looking for an entity by a non-composite key.");
        } else {
            return entryPoint.condition(buildConditionForKeyMember(AbstractEntity.KEY, getKeyType(), keyValues[0]));
        }
    }

    private ConditionModel buildConditionForKeyMember(final String propName, final Class propType, final Object propValue) {
        if (propValue == null) {
            return cond().prop(propName).isNull().model();
        } else if (String.class.equals(propType)) {
            return cond().lowerCase().prop(propName).eq().lowerCase().val(propValue).model();
        } else if (Class.class.equals(propType)) {
            return cond().prop(propName).eq().val(((Class<?>) propValue).getName()).model();
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
    
    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }
 
    /**
     * Instantiates an instrumented new entity of the type for which this object is a companion.
     * The default entity constructor, which should be protected, is used for instantiation.
     *
     * @return
     */
    @Override
    public T new_() {
        return entityFactory.newEntity(getEntityType());
    }

}