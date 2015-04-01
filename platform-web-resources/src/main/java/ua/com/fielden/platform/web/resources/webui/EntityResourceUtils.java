package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * This utility class contains the methods that are shared across {@link EntityResource} and {@link EntityValidationResource}.
 *
 * @author TG Team
 *
 */
public class EntityResourceUtils<T extends AbstractEntity<?>> {
    private final EntityFactory entityFactory;
    private final static Logger logger = Logger.getLogger(EntityResourceUtils.class);
    private final Class<T> entityType;
    private final IEntityDao<T> dao;
    private final IEntityProducer<T> entityProducer;
    private final ICompanionObjectFinder companionFinder;

    public EntityResourceUtils(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder) {
        this.entityType = entityType;
        this.companionFinder = companionFinder;
        this.dao = companionFinder.<IEntityDao<T>, T> find(this.entityType);

        this.entityFactory = entityFactory;
        this.entityProducer = entityProducer;
    }

    /**
     * Initialises the entity for retrieval.
     *
     * @param id
     *            -- entity identifier
     * @return
     */
    public T createValidationPrototype(final Long id) {
        final T entity;
        if (id != null) {
            entity = dao.findById(id, dao.getFetchProvider().fetchModel());
        } else {
            entity = entityProducer.newEntity();
        }
        return entity;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public ICompanionObjectFinder getCompanionFinder() {
        return companionFinder;
    }

    public static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IFetchProvider<V> fetchForProperty(final ICompanionObjectFinder coFinder, final Class<T> entityType, final String propertyName) {
        if (EntityQueryCriteria.class.isAssignableFrom(entityType)) {
            final Class<? extends AbstractEntity<?>> originalType = CentreResourceUtils.getOriginalType(entityType);
            final String originalPropertyName = CentreResourceUtils.getOriginalPropertyName(entityType, propertyName);

            final boolean isEntityItself = "".equals(originalPropertyName); // empty property means "entity itself"
            return isEntityItself ? (IFetchProvider<V>) coFinder.find(originalType).getFetchProvider() : coFinder.find(originalType).getFetchProvider().fetchFor(originalPropertyName);
        } else {
            return coFinder.find(entityType).getFetchProvider().fetchFor(propertyName);
        }
    }

    /**
     * Determines the version that is shipped with 'modifiedPropertiesHolder'.
     *
     * @param modifiedPropertiesHolder
     * @return
     */
    public static Long getVersion(final Map<String, Object> modifiedPropertiesHolder) {
        final Object arrivedVersionVal = modifiedPropertiesHolder.get(AbstractEntity.VERSION);
        return ((Integer) arrivedVersionVal).longValue();
    }

    /**
     * Applies the values from <code>dirtyPropertiesHolder</code> into the <code>entity</code>. The values needs to be converted from the client-side component-specific form into
     * the values, which can be set into Java entity's property.
     *
     * @param modifiedPropertiesHolder
     * @param entity
     * @return
     */
    public static <M extends AbstractEntity<?>> M apply(final Map<String, Object> modifiedPropertiesHolder, final M entity, final ICompanionObjectFinder companionFinder) {
        final Class<M> type = (Class<M>) entity.getType();
        final boolean isEntityStale = entity.getVersion() > getVersion(modifiedPropertiesHolder);

        // iterate through modified properties:
        for (final Map.Entry<String, Object> nameAndVal : modifiedPropertiesHolder.entrySet()) {
            final String name = nameAndVal.getKey();
            if (!name.equals(AbstractEntity.ID) && !name.equals(AbstractEntity.VERSION) && !name.startsWith("@") /* custom properties disregarded */) {
                final Map<String, Object> valAndOrigVal = (Map<String, Object>) nameAndVal.getValue();
                // The 'modified' properties are marked using the existence of "val" sub-property.
                if (valAndOrigVal.containsKey("val")) { // this is a modified property
                    final Object newValue = convert(type, name, valAndOrigVal.get("val"), companionFinder);
                    if (notFoundEntity(type, name, valAndOrigVal.get("val"), newValue)) {
                        final String msg = String.format("The entity has not been found for [%s].", valAndOrigVal.get("val"));
                        logger.info(msg);
                        entity.getProperty(name).setDomainValidationResult(Result.failure(entity, msg));
                    } else if (multipleFoundEntities(type, name, valAndOrigVal.get("val"), newValue)) {
                        final String msg = String.format("Multiple entities have been found for [%s].", valAndOrigVal.get("val"));
                        logger.info(msg);
                        entity.getProperty(name).setDomainValidationResult(Result.failure(entity, msg));
                    } else if (!isEntityStale) {
                        entity.set(name, newValue);
                    } else {
                        final Object staleOriginalValue = convert(type, name, valAndOrigVal.get("origVal"), companionFinder);
                        if (EntityUtils.isConflicting(newValue, staleOriginalValue, entity.get(name))) {
                            final String msg = "The property has been recently changed by other user. Please revert property value to resolve conflict.";
                            logger.info(msg);
                            entity.getProperty(name).setDomainValidationResult(Result.failure(entity, msg));
                        } else {
                            entity.set(name, newValue);
                        }
                    }
                } else { // this is unmodified property
                    if (!isEntityStale) {
                        // do nothing
                    } else {
                        final Object originalValue = convert(type, name, valAndOrigVal.get("origVal"), companionFinder);
                        if (EntityUtils.isStale(originalValue, entity.get(name))) {
                            final String msg = "The property has been recently changed by other user.";
                            logger.info(msg);
                            entity.getProperty(name).setDomainValidationResult(Result.warning(entity, msg));
                        }
                    }
                }
            }
        }
        // IMPORTANT: the check for invalid will populate 'required' checks.
        //            It is necessary in case when some property becomes required after the change of other properties.
        entity.isValid();

        return entity;
    }

    /**
     * Returns <code>true</code> if the property is of entity type and the entity was not found by the search string (reflectedValue), <code>false</code> otherwise.
     *
     * @param type
     * @param propertyName
     * @param reflectedValue
     * @param newValue
     * @return
     */
    private static <M extends AbstractEntity<?>> boolean notFoundEntity(final Class<M> type, final String propertyName, final Object reflectedValue, final Object newValue) {
        return reflectedValue != null && newValue == null && EntityUtils.isEntityType(PropertyTypeDeterminator.determinePropertyType(type, propertyName));
    }

    /**
     * Returns <code>true</code> if the property is of entity type and multiple entities ware found by the search string (reflectedValue), <code>false</code> otherwise.
     *
     * @param type
     * @param propertyName
     * @param reflectedValue
     * @param newValue
     * @return
     */
    private static <M extends AbstractEntity<?>> boolean multipleFoundEntities(final Class<M> type, final String propertyName, final Object reflectedValue, final Object newValue) {
        return reflectedValue != null && Arrays.asList().equals(newValue) && EntityUtils.isEntityType(PropertyTypeDeterminator.determinePropertyType(type, propertyName));
    }

    /**
     * Converts <code>reflectedValue</code> (which is a string, number, boolean or null) into a value of appropriate type (the type of actual property).
     *
     * @param type
     * @param propertyName
     * @param reflectedValue
     * @return
     */
    private static <M extends AbstractEntity<?>> Object convert(final Class<M> type, final String propertyName, final Object reflectedValue, final ICompanionObjectFinder companionFinder) {
        final Class propertyType = PropertyTypeDeterminator.determinePropertyType(type, propertyName);

        // NOTE: "missing value" for Java entities is also 'null' as for JS entities
        if (EntityUtils.isEntityType(propertyType)) {
            if (reflectedValue == null) {
                return null;
            }
            final Class<AbstractEntity<?>> entityPropertyType = propertyType;

            final IEntityDao<AbstractEntity<?>> propertyCompanion = companionFinder.<IEntityDao<AbstractEntity<?>>, AbstractEntity<?>> find(entityPropertyType);

            final EntityResultQueryModel<AbstractEntity<?>> model = select(entityPropertyType).where().//
            /*      */prop(AbstractEntity.KEY).iLike().anyOfValues((Object[]) MiscUtilities.prepare(Arrays.asList((String) reflectedValue))).//
            /*      */model();
            final QueryExecutionModel<AbstractEntity<?>, EntityResultQueryModel<AbstractEntity<?>>> qem = from(model).with(fetchForProperty(companionFinder, type, propertyName).fetchModel()).model();
            try {
                return propertyCompanion.getEntity(qem);
            } catch (final IllegalArgumentException e) {
                if (e.getMessage().equals("The provided query model leads to retrieval of more than one entity (2).")) {
                    return Arrays.asList();
                } else {
                    throw e;
                }
            }
            // prev implementation => return propertyCompanion.findByKeyAndFetch(getFetchProvider().fetchFor(propertyName).fetchModel(), reflectedValue);
        } else if (EntityUtils.isString(propertyType)) {
            return reflectedValue == null ? null : reflectedValue;
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            return reflectedValue == null ? null : reflectedValue;
        } else if (EntityUtils.isBoolean(propertyType)) {
            return reflectedValue == null ? null : reflectedValue;
        } else if (EntityUtils.isDate(propertyType)) {
            return reflectedValue == null ? null : (reflectedValue instanceof Integer ? new Date(((Integer) reflectedValue).longValue()) : new Date((Long) reflectedValue));
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            return reflectedValue == null ? null : (reflectedValue instanceof Integer ? new BigDecimal((Integer) reflectedValue) : new BigDecimal((Double) reflectedValue));
        } else if (Money.class.isAssignableFrom(propertyType)) {
            if (reflectedValue == null) {
                return null;
            }
            final Map<String, Object> map = (Map<String, Object>) reflectedValue;

            final BigDecimal amount = map.get("amount") instanceof Integer ? new BigDecimal((Integer) map.get("amount")) : new BigDecimal((Double) map.get("amount"));
            final String currencyStr = (String) map.get("currency");
            final Integer taxPercentage = (Integer) map.get("taxPercent");

            return taxPercentage == null ? new Money(amount, Currency.getInstance(currencyStr)) : new Money(amount, taxPercentage, Currency.getInstance(currencyStr));
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported conversion to [%s + %s] from reflected value [%s].", type.getSimpleName(), propertyName, reflectedValue));
        }
    }

    /**
     * Restores the holder of modified properties into the map [propertyName; webEditorSpecificValue].
     *
     * @param envelope
     * @return
     */
    public static Map<String, Object> restoreModifiedPropertiesHolderFrom(final Representation envelope, final RestServerUtil restUtil) {
        try {
            return (Map<String, Object>) restUtil.restoreJSONMap(envelope);
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during deserialisation of modified properties holder, which should be validated.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Just saves the entity.
     *
     * @param entity
     * @return
     */
    public T save(final T entity) {
        return dao.save(entity);
    }

    /**
     * Deletes the entity.
     *
     * @param entityId
     */
    public void delete(final Long entityId) {
        dao.delete(entityFactory.newEntity(entityType, entityId));
    }

    /**
     * Constructs the entity from the client envelope.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @param envelope
     * @param restUtil
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public Pair<T, Map<String, Object>> constructEntity(final Map<String, Object> modifiedPropertiesHolder, final Long id) {
        return constructEntity(modifiedPropertiesHolder, createValidationPrototype(id), getCompanionFinder());
    }

    /**
     * Constructs the entity from the client envelope.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> constructEntity(final Map<String, Object> modifiedPropertiesHolder, final M validationPrototype, final ICompanionObjectFinder companionFinder) {
        return new Pair<>(apply(modifiedPropertiesHolder, validationPrototype, companionFinder), modifiedPropertiesHolder);
    }

    /**
     * Constructs the entity from the client envelope and resets the original values of the entity to be equal to the values.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> constructEntityAndResetMetaValues(final Map<String, Object> modifiedPropertiesHolder, final M validationPrototype, final ICompanionObjectFinder companionFinder) {
        return new Pair<>((M) apply(modifiedPropertiesHolder, validationPrototype, companionFinder).resetMetaState(), modifiedPropertiesHolder);
    }

    /**
     * Constructs the entity from the client envelope.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public Pair<T, Map<String, Object>> constructEntity(final Map<String, Object> modifiedPropertiesHolder) {
        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : ((Integer) arrivedIdVal).longValue();

        return constructEntity(modifiedPropertiesHolder, id);
    }
}
