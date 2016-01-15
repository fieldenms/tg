package ua.com.fielden.platform.web.resources.webui;

import static java.util.Locale.getDefault;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.sample.domain.TgUpdateRolesAction;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUtils;
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
    private final IEntityDao<T> co;
    private final IEntityProducer<T> entityProducer;
    private final ICompanionObjectFinder companionFinder;

    public EntityResourceUtils(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final ICompanionObjectFinder companionFinder) {
        this.entityType = entityType;
        this.companionFinder = companionFinder;
        this.co = companionFinder.<IEntityDao<T>, T> find(this.entityType);

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
            entity = co.findById(id, co.getFetchProvider().fetchModel());
        } else {
            entity = entityProducer.newEntity();
        }
        return entity;
    }

    /**
     * Initialises the functional entity for centre-context-dependent retrieval.
     *
     * @param centreContext
     *            the context for functional entity creation
     *
     * @return
     */
    public T createValidationPrototypeWithContext(
            final Long id,
            final CentreContext<T, AbstractEntity<?>> centreContext,
            final String chosenProperty,
            final Long compoundMasterEntityId,
            final AbstractEntity<?> masterContext) {
        if (id != null) {
            return co.findById(id, co.getFetchProvider().fetchModel());
        } else {
            final DefaultEntityProducerWithContext<T, T> defProducer = (DefaultEntityProducerWithContext<T, T>) entityProducer;
            defProducer.setCentreContext(centreContext);
            defProducer.setChosenProperty(chosenProperty);
            defProducer.setCompoundMasterEntityId(compoundMasterEntityId);
            defProducer.setMasterEntity(masterContext);
            return defProducer.newEntity();
        }
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public ICompanionObjectFinder getCompanionFinder() {
        return companionFinder;
    }

    public static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IFetchProvider<V> fetchForProperty(final ICompanionObjectFinder coFinder, final Class<T> entityType, final String propertyName) {
        if (EntityQueryCriteria.class.isAssignableFrom(entityType)) {
            final Class<? extends AbstractEntity<?>> originalType = CentreUtils.getOriginalType(entityType);
            final String originalPropertyName = CentreUtils.getOriginalPropertyName(entityType, propertyName);

            final boolean isEntityItself = "".equals(originalPropertyName); // empty property means "entity itself"
            return isEntityItself ? (IFetchProvider<V>) coFinder.find(originalType).getFetchProvider() : fetchForPropertyOrDefault(coFinder, originalType, originalPropertyName);
        } else {
            return coFinder.find(entityType).getFetchProvider().fetchFor(propertyName);
        }
    }

    /**
     * Returns fetch provider for property or, if the property should not be fetched according to default strategy, returns the 'default' property fetch provider with 'keys'
     * (simple an composite) and 'desc' (if 'desc' exists in domain entity).
     *
     * @param coFinder
     * @param entityType
     * @param propertyName
     * @return
     */
    private static <V extends AbstractEntity<?>> IFetchProvider<V> fetchForPropertyOrDefault(final ICompanionObjectFinder coFinder, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        final IFetchProvider<? extends AbstractEntity<?>> fetchProvider = coFinder.find(entityType).getFetchProvider();
        //        return fetchProvider.fetchFor(propertyName);
        return fetchProvider.shouldFetch(propertyName)
                ? fetchProvider.fetchFor(propertyName)
                : fetchProvider.with(propertyName).fetchFor(propertyName);
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
     * Applies the values from <code>modifiedPropertiesHolder</code> into the <code>entity</code>. The values needs to be converted from the client-side component-specific form into
     * the values, which can be set into Java entity's property.
     *
     * @param modifiedPropertiesHolder
     * @param entity
     * @return
     */
    public static <M extends AbstractEntity<?>> M apply(final Map<String, Object> modifiedPropertiesHolder, final M entity, final ICompanionObjectFinder companionFinder) {
        final Class<M> type = (Class<M>) entity.getType();
        final boolean isEntityStale = entity.getVersion() > getVersion(modifiedPropertiesHolder);

        final Set<String> appliedProps = new LinkedHashSet<>();
        final List<String> touchedProps = (List<String>) modifiedPropertiesHolder.get("@@touchedProps");
        
        // iterate through untouched properties first:
        //  (the order of application does not really matter - untouched properties were really applied earlier through some definers, that originate from touched properties)
        for (final Map.Entry<String, Object> nameAndVal : modifiedPropertiesHolder.entrySet()) {
            final String name = nameAndVal.getKey();
            if (!name.equals(AbstractEntity.ID) && !name.equals(AbstractEntity.VERSION) && !name.startsWith("@") /* custom properties disregarded */ && !touchedProps.contains(name)) {
                final Map<String, Object> valAndOrigVal = (Map<String, Object>) nameAndVal.getValue();
                // The 'modified' properties are marked using the existence of "val" sub-property.
                if (valAndOrigVal.containsKey("val")) { // this is a modified property
                    applyModifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale);
                    appliedProps.add(name);
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
        // iterate through touched properties:
        //  (the order of application is strictly the same as was done by the user in Web UI client - the only difference is 
        //  such that properties, that were touched twice or more times, will be applied only once)
        for (final String touchedProp : touchedProps) {
            final String name = touchedProp;
            final Map<String, Object> valAndOrigVal = (Map<String, Object>) modifiedPropertiesHolder.get(name);
            // The 'modified' properties are marked using the existence of "val" sub-property.
            if (valAndOrigVal.containsKey("val")) { // this is a modified property
                applyModifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale);
            } else { // this is unmodified property
                // IMPORTANT:
                // Unlike to the case of untouched properties, all touched properties should be applied,
                //  even unmodified ones.
                // This is necessary in order to mimic the user interaction with the entity (like was in Swing client)
                //  to have the ACE handlers executed for all touched properties.
                applyUnmodifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale);
            }
            appliedProps.add(name);
        }
        // IMPORTANT: the check for invalid will populate 'required' checks.
        //            It is necessary in case when some property becomes required after the change of other properties.
        entity.isValid();

        disregardCritOnlyRequiredProperties(entity);
        disregardNotAppliedRequiredProperties(entity, appliedProps);

        return entity;
    }
    
    /**
     * Applies the property value against the entity.
     * 
     * @param shouldApplyOriginalValue - indicates whether the 'origVal' should be applied (with 'enforced mutation') or 'val' (with simple mutation)
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     */
    private static <M extends AbstractEntity<?>> void applyPropertyValue(final boolean shouldApplyOriginalValue, final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale) {
        final Object val = shouldApplyOriginalValue ? valAndOrigVal.get("origVal") : valAndOrigVal.get("val");
        final Object newValue = convert(type, name, val, companionFinder);
        if (notFoundEntity(type, name, val, newValue)) {
            final String msg = String.format("The entity has not been found for [%s].", val);
            logger.info(msg);
            entity.getProperty(name).setDomainValidationResult(Result.failure(entity, msg));
        } else if (multipleFoundEntities(type, name, val, newValue)) {
            final String msg = String.format("Multiple entities have been found for [%s].", val);
            logger.info(msg);
            entity.getProperty(name).setDomainValidationResult(Result.failure(entity, msg));
        } else if (!isEntityStale) {
            enforceSet(shouldApplyOriginalValue, name, entity, newValue);
        } else {
            final Object staleOriginalValue = convert(type, name, valAndOrigVal.get("origVal"), companionFinder);
            if (EntityUtils.isConflicting(newValue, staleOriginalValue, entity.get(name))) {
                final String msg = "The property has been recently changed by other user. Please revert property value to resolve conflict.";
                logger.info(msg);
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // TODO THIS IS VERY IMPORTANT LINE, IT SHOULD BE UNCOMMENTED
                // entity.getProperty(name).setDomainValidationResult(Result.failure(entity, msg));
            } else {
                enforceSet(shouldApplyOriginalValue, name, entity, newValue);
            }
        }
    }
    
    /**
     * Applies the modified property value ('val') against the entity.
     * 
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     */
    private static <M extends AbstractEntity<?>> void applyModifiedPropertyValue(final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale) {
        applyPropertyValue(false, type, name, valAndOrigVal, entity, companionFinder, isEntityStale);
    }
    
    /**
     * Applies the unmodified property value ('origVal') against the entity (using 'enforced mutation').
     * 
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     */
    private static <M extends AbstractEntity<?>> void applyUnmodifiedPropertyValue(final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale) {
        applyPropertyValue(true, type, name, valAndOrigVal, entity, companionFinder, isEntityStale);
    }

    /**
     * Sets the value for the entity property.
     * 
     * @param enforce - indicates whether to use 'enforced mutation'
     * @param name
     * @param entity
     * @param newValue
     */
    private static <M extends AbstractEntity<?>> void enforceSet(final boolean enforce, final String name, final M entity, final Object newValue) {
        if (enforce) {
            final MetaProperty<Object> metaProperty = entity.getProperty(name);
            final boolean currEnforceMutator = metaProperty.isEnforceMutator();
            metaProperty.setEnforceMutator(true);
            try {
                entity.set(name, newValue);
            } finally {
                metaProperty.setEnforceMutator(currEnforceMutator);
            }
        } else {
            entity.set(name, newValue);
        }
    }

    /**
     * Disregards the 'required' errors for those properties, that were not 'applied' (for both criteria and simple entities).
     *
     * @param entity
     * @param appliedProps
     *            -- list of 'applied' properties, i.e. those for which the setter has been invoked (maybe in 'enforced' manner)
     * @return
     */
    public static <M extends AbstractEntity<?>> M disregardNotAppliedRequiredProperties(final M entity, final Set<String> appliedProps) {
        for (final Map.Entry<String, MetaProperty<?>> entry : entity.getProperties().entrySet()) {
            if (entry.getValue().isRequired() && !appliedProps.contains(entry.getKey())) {
                entry.getValue().setRequiredValidationResult(Result.successful(entity));
            }
        }
        return entity;
    }

    /**
     * Disregards the 'required' errors for crit-only properties on masters for non-criteria entity types.
     * 
     * @param entity
     */
    public static <M extends AbstractEntity<?>> void disregardCritOnlyRequiredProperties(final M entity) {
        final Class<?> managedType = entity.getType();
        if (!EntityQueryCriteria.class.isAssignableFrom(managedType)) {
            for (final Map.Entry<String, MetaProperty<?>> entry : entity.getProperties().entrySet()) {
                if (entry.getValue().isRequired()) {
                    final String prop = entry.getKey();
                    final CritOnly critOnlyAnnotation = AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, prop);
                    if (critOnlyAnnotation != null) {
                        entry.getValue().setRequiredValidationResult(Result.successful(entity));
                    }
                }
            }
        }
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
        if (reflectedValue == null) {
            return null;
        }
        final Class propertyType = PropertyTypeDeterminator.determinePropertyType(type, propertyName);

        // NOTE: "missing value" for Java entities is also 'null' as for JS entities
        if (EntityUtils.isEntityType(propertyType)) {
            if (PropertyTypeDeterminator.isCollectional(type, propertyName)) {
                throw new UnsupportedOperationException(String.format("Unsupported conversion to [%s + %s] from reflected value [%s]. Collectional properties are not supported.", type.getSimpleName(), propertyName, reflectedValue));
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
            return reflectedValue;
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            return reflectedValue;
        } else if (EntityUtils.isBoolean(propertyType)) {
            return reflectedValue;
        } else if (EntityUtils.isDate(propertyType)) {
            return reflectedValue instanceof Integer ? new Date(((Integer) reflectedValue).longValue()) : new Date((Long) reflectedValue);
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            final MapTo mapTo = AnnotationReflector.getPropertyAnnotation(MapTo.class, type, propertyName);
            final CritOnly critOnly = AnnotationReflector.getPropertyAnnotation(CritOnly.class, type, propertyName);
            final Integer propertyScale = mapTo != null && mapTo.scale() >= 0 ? ((int) mapTo.scale())
                    : (critOnly != null && critOnly.scale() >= 0 ? ((int) critOnly.scale()) : 2)/* default value from Hibernate */;

            if (reflectedValue instanceof Integer) {
                return new BigDecimal((Integer) reflectedValue).setScale(propertyScale, RoundingMode.HALF_UP);
            } else if (reflectedValue instanceof Long) {
                return BigDecimal.valueOf((Long) reflectedValue).setScale(propertyScale, RoundingMode.HALF_UP);
            } else if (reflectedValue instanceof BigInteger) {
                return new BigDecimal((BigInteger) reflectedValue).setScale(propertyScale, RoundingMode.HALF_UP);
            } else if (reflectedValue instanceof BigDecimal) {
                return ((BigDecimal) reflectedValue).setScale(propertyScale, RoundingMode.HALF_UP);
            } else {
                throw new IllegalStateException("Unknown number type for 'reflectedValue'.");
            }
        } else if (Money.class.isAssignableFrom(propertyType)) {
            final Map<String, Object> map = (Map<String, Object>) reflectedValue;

            final BigDecimal amount = new BigDecimal(map.get("amount").toString());
            final String currencyStr = (String) map.get("currency");
            final Integer taxPercentage = (Integer) map.get("taxPercent");

            if (taxPercentage == null) {
                if (StringUtils.isEmpty(currencyStr)) {
                    return new Money(amount);
                } else {
                    return new Money(amount, Currency.getInstance(currencyStr));
                }
            } else {
                if (StringUtils.isEmpty(currencyStr)) {
                    return new Money(amount, taxPercentage,  Currency.getInstance(getDefault()));
                } else {
                    return new Money(amount, taxPercentage, Currency.getInstance(currencyStr));
                }
            }

        } else if (Colour.class.isAssignableFrom(propertyType)) {
            final Map<String, Object> map = (Map<String, Object>) reflectedValue;

            final String hashlessUppercasedColourValue = (String) map.get("hashlessUppercasedColourValue");
            return hashlessUppercasedColourValue == null ? null : new Colour(hashlessUppercasedColourValue);
        } else if (PropertyTypeDeterminator.isCollectional(type, propertyName) && Set.class.isAssignableFrom(Finder.findFieldByName(type, propertyName).getType()) && Long.class.isAssignableFrom(propertyType)) {
            final List<Object> list = (ArrayList<Object>) reflectedValue;
            final Set<Long> resultSet = new LinkedHashSet<>();
            for (final Object entry : list) {
                if (entry == null) {
                    resultSet.add(null);
                } else {
                    resultSet.add(Long.parseLong(entry.toString()));
                }
            }
            return resultSet;
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
     * Restores the holder of context and criteria entity.
     *
     * @param envelope
     * @return
     */
    public static CentreContextHolder restoreCentreContextHolder(final Representation envelope, final RestServerUtil restUtil) {
        try {
            return restUtil.restoreJSONEntity(envelope, CentreContextHolder.class);
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during deserialisation of centre context holder, which should be validated.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Restores the {@link Result} from JSON envelope.
     *
     * @param envelope
     * @return
     */
    public static Result restoreJSONResult(final Representation envelope, final RestServerUtil restUtil) {
        try {
            return restUtil.restoreJSONResult(envelope);
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during deserialisation of Result.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Restores the holder of saving information (modified props + centre context, if any).
     *
     * @param envelope
     * @return
     */
    public static SavingInfoHolder restoreSavingInfoHolder(final Representation envelope, final RestServerUtil restUtil) {
        try {
            return restUtil.restoreJSONEntity(envelope, SavingInfoHolder.class);
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during deserialisation of SavingInfoHolder, which should be validated.", ex);
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
        return co.save(entity);
    }

    /**
     * Deletes the entity.
     *
     * @param entityId
     */
    public void delete(final Long entityId) {
        co.delete(entityFactory.newEntity(entityType, entityId));
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
     * @param envelope
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    public Pair<T, Map<String, Object>> constructEntity(
            final Map<String, Object> modifiedPropertiesHolder,
            final CentreContext<T, AbstractEntity<?>> centreContext,
            final String chosenProperty,
            final Long compoundMasterEntityId,
            final AbstractEntity<?> masterContext) {

        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");

        return constructEntity(modifiedPropertiesHolder, createValidationPrototypeWithContext(id, centreContext, chosenProperty, compoundMasterEntityId, masterContext), getCompanionFinder());
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
    private static <M extends AbstractEntity<?>> Pair<M, Map<String, Object>> constructEntity(final Map<String, Object> modifiedPropertiesHolder, final M validationPrototype, final ICompanionObjectFinder companionFinder) {
        return new Pair<>(apply(modifiedPropertiesHolder, validationPrototype, companionFinder), modifiedPropertiesHolder);
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
        final Long id = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");

        return constructEntity(modifiedPropertiesHolder, id);
    }

    public EntityFactory entityFactory() {
        return entityFactory;
    }

    /**
     * This method wraps the function of representation creation to handle properly <b>undesired</b> server errors.
     * <p>
     * Please note that all <b>expected</b> exceptional situations should be handled inside the respective 'representationCreator' and one should not rely on this method for such
     * errors.
     *
     * @param representationCreator
     * @return
     */
    public static Representation handleUndesiredExceptions(final Response response, final Supplier<Representation> representationCreator, final RestServerUtil restUtil) {
        try {
            return representationCreator.get();
        } catch (final Exception undesiredEx) {
            logger.error(undesiredEx.getMessage(), undesiredEx);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.errorJSONRepresentation(undesiredEx);
        }
    }
}
