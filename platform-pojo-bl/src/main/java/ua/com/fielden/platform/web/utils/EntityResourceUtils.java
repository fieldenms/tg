package ua.com.fielden.platform.web.utils;

import static java.lang.String.format;
import static java.util.Locale.getDefault;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.EntityJsonDeserialiser;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.ui.menu.MiType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;

/**
 * This utility class contains the methods that are shared across EntityResource and EntityValidationResource.
 *
 * @author TG Team
 *
 */
public class EntityResourceUtils {
    private static final String CONFLICT_WARNING = "This property has recently been changed by another user.";
    public static final String CENTRE_CONFIG_CONFLICT_WARNING = "Configuration with this title already exists.";
    public static final String CENTRE_CONFIG_CONFLICT_ERROR = "Base " + uncapitalize(CENTRE_CONFIG_CONFLICT_WARNING);
    private static final String RESOLVE_CONFLICT_INSTRUCTION = "Please either edit the value back to [%s] to resolve the conflict or cancel all of your changes.";
    /**
     * Used to indicate the start of 'not found mock' serialisation sequence.
     */
    private static final String NOT_FOUND_MOCK_PREFIX = "__________NOT_FOUND__________";
    private static final Logger logger = Logger.getLogger(EntityResourceUtils.class);
    /**
     * Standard {@link PropertyDescriptor}'s convertor to string. Includes handling for 'not found mock' instances.
     */
    public static final Function<PropertyDescriptor<?>, String> PROPERTY_DESCRIPTOR_TO_STRING = entity -> entityWithMocksToString(pd -> pd.toString(), entity);
    /**
     * Standard {@link PropertyDescriptor}'s convertor from string. Includes handling for 'not found mock' instances.
     * <p>
     * Note that this is applicable only to restore uninstrumented {@link PropertyDescriptor}s. This is common case of {@link PropertyDescriptor} usage -- as a property value of some other entity.
     * However, we also have similar logic in {@link EntityJsonDeserialiser}, which also deserialises instrumented instances.
     */
    public static final Function<String, PropertyDescriptor<?>> PROPERTY_DESCRIPTOR_FROM_STRING = str -> entityWithMocksFromString(PropertyDescriptor::fromString, str, PropertyDescriptor.class);
    
    private EntityResourceUtils() {}
    
    public static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IFetchProvider<V> fetchForProperty(final ICompanionObjectFinder coFinder, final Class<T> entityType, final String propertyName) {
        if (EntityQueryCriteria.class.isAssignableFrom(entityType)) {
            final Class<? extends AbstractEntity<?>> originalType = EntityResourceUtils.getOriginalType(entityType);
            final String originalPropertyName = EntityResourceUtils.getOriginalPropertyName(entityType, propertyName);

            final boolean isEntityItself = "".equals(originalPropertyName); // empty property means "entity itself"
            return isEntityItself ? (IFetchProvider<V>) coFinder.find(originalType).getFetchProvider() : fetchForPropertyOrDefault(coFinder, originalType, originalPropertyName);
        } else {
            return coFinder.find(entityType).getFetchProvider().fetchFor(propertyName);
        }
    }
    
    /**
     * Returns <code>true</code> in case where the <code>warning</code> does not represent special 'conflicting' warning, <code>false</code> otherwise.
     * 
     * @param warning
     * @return
     */
    public static boolean isNonConflicting(final Warning warning) {
        return !CONFLICT_WARNING.equals(warning.getMessage()) && !CENTRE_CONFIG_CONFLICT_WARNING.equals(warning.getMessage());
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
        return fetchForPropertyOrDefault(fetchProvider, propertyName);
    }
    
    /**
     * Returns fetch provider for property or, if the property should not be fetched according to default strategy, returns the 'default' property fetch provider with 'keys'
     * (simple an composite) and 'desc' (if 'desc' exists in domain entity).
     *
     * @param fetchProvider
     * @param propertyName
     * @return
     */
    public static <V extends AbstractEntity<?>> IFetchProvider<V> fetchForPropertyOrDefault(final IFetchProvider<? extends AbstractEntity<?>> fetchProvider, final String propertyName) {
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
        final boolean isCriteriaEntity = EntityQueryCriteria.class.isAssignableFrom(type);

        final Set<String> touchedProps = new LinkedHashSet<>((List<String>) modifiedPropertiesHolder.get("@@touchedProps"));

        // iterate through untouched properties first:
        //  (the order of application does not really matter - untouched properties were really applied earlier through some definers, that originate from touched properties)
        for (final Map.Entry<String, Object> nameAndVal : modifiedPropertiesHolder.entrySet()) {
            final String name = nameAndVal.getKey();
            if (!name.equals(AbstractEntity.ID) && !name.equals(AbstractEntity.VERSION) && !name.startsWith("@") /* custom properties disregarded */ && !touchedProps.contains(name)) {
                final Map<String, Object> valAndOrigVal = (Map<String, Object>) nameAndVal.getValue();
                // The 'modified' properties are marked using the existence of "val" sub-property.
                if (valAndOrigVal.containsKey("val")) { // this is a modified property
                    applyModifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
                    // logPropertyApplication("   Apply untouched   modified", true, true, type, name, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
                } else { // this is unmodified property
                    // IMPORTANT:
                    // Untouched properties should not be applied, but validation for conflicts should be performed.
                    validateUnmodifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
                    // logPropertyApplication("Validate untouched unmodified", false, true, type, name, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
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
                applyModifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
                // logPropertyApplication("   Apply   touched   modified", true, true, type, name, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
            } else { // this is unmodified property
                // IMPORTANT:
                // Unlike to the case of untouched properties, all touched properties should be applied,
                //  even unmodified ones.
                // This is necessary in order to mimic the user interaction with the entity (like was in Swing client)
                //  to have the ACE handlers executed for all touched properties.
                applyUnmodifiedPropertyValue(type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
                // logPropertyApplication("   Apply   touched unmodified", true, true, type, name, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
            }
        }
        // IMPORTANT: the check for invalid will populate 'required' checks.
        //            It is necessary in case when some property becomes required after the change of other properties.
        entity.isValid();

        disregardCritOnlyRequiredProperties(entity);
        disregardUntouchedRequiredProperties(entity, touchedProps);
        disregardTouchedRequiredPropertiesWithEmptyValue(entity, touchedProps);

        return entity;
    }
    
    /**
     * Logs property application / validation in a table form to easily debug data flow in method 'apply'.
     * 
     * @param actionCaption
     * @param apply
     * @param shortLog -- specifies shorter or wider (with 'type' and staleness) view of information
     * @param type
     * @param name
     * @param isEntityStale
     * @param valAndOrigVal
     * @param entity
     * @param propertiesToLogArray -- specifies what properties are interested
     */
    @SuppressWarnings("unused")
    private static <M extends AbstractEntity<?>> void logPropertyApplication(final String actionCaption, final boolean apply, final boolean shortLog, final Class<M> type, final String name, final boolean isEntityStale, final Map<String, Object> valAndOrigVal, final M entity, final String... propertiesToLogArray) {
        final Set<String> propertiesToLog = new LinkedHashSet<>(Arrays.asList(propertiesToLogArray));
        if (propertiesToLog.contains(name)) {
            final StringBuilder builder = new StringBuilder(actionCaption);
            builder.append(":\t");
            if (!shortLog) {
                builder.append(format("type [%40s] ", type.getSimpleName()));
            }
            builder.append(format("name [%8s] ", name));
            if (!shortLog) {
                builder.append(format("isEntityStale [%8s] ", isEntityStale));
            }
            builder.append(format("val [%8s] ", valAndOrigVal.getOrDefault("val", "")));
            builder.append(format("origVal [%8s] ", valAndOrigVal.get("origVal")));
            if (apply) {
                builder.append("=>\t");
                for (final String propertyToLog: propertiesToLog) {
                    builder.append(format("%8s = %8s ", propertyToLog, entity.get(propertyToLog)));
                }
            }
            System.out.println(builder.toString()); // use logger instead of sysout if needed
        }
    }
    
    /**
     * Validates / applies the property value against the entity.
     *
     * @param apply - indicates whether property application should be performed; if <code>false</code> then only validation will be performed
     * @param shouldApplyOriginalValue - indicates whether the 'origVal' should be applied or 'val'
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     * @param isCriteriaEntity
     */
    private static <M extends AbstractEntity<?>> void processPropertyValue(final boolean apply, final boolean shouldApplyOriginalValue, final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale, final boolean isCriteriaEntity) {
        if (apply) {
            // in case where application is necessary (modified touched, modified untouched, unmodified touched) the value (valueToBeApplied) should be checked on existence and then (if successful) it should be applied
            final String valueToBeAppliedName = shouldApplyOriginalValue ? "origVal" : "val";
            final Object valToBeApplied = valAndOrigVal.get(valueToBeAppliedName);
            final Object convertedValue = convert(type, name, valToBeApplied, reflectedValueId(valAndOrigVal, valueToBeAppliedName), companionFinder);
            final Object valueToBeApplied;
            if (valToBeApplied != null && convertedValue == null) {
                final Class<?> propType = determinePropertyType(type, name);
                if (isEntityType(propType)) {
                    valueToBeApplied = createMockNotFoundEntity(propType, (String) valToBeApplied); // here valToBeApplied must be string; look at 'convert' method with 'reflectedValue' parameter always string for entity-typed 'propertyType'
                } else {
                    valueToBeApplied = convertedValue;
                }
            } else {
                valueToBeApplied = convertedValue;
            }
            validateAnd(() -> {
                // Value application should be enforced.
                // This is necessary not only for 'touched unmodified' properties (made earlier), but also for 'touched modified' and 'untouched modified' (new logic, 2017-12).
                // This is necessary because without enforcement property application (with respective definers execution) could be avoided for seemingly 'modified' properties.
                // This is due to the fact that 'modified' property value is always different from original value, but could be equal to the actual value of the property immediately before application.
                // This situation occurs where the property was modified indirectly from definers of other properties in method 'apply'.
                // 'enforce == true' guarantees that property application with validators / definers will always be actioned.
                entity.getProperty(name).setValue(valueToBeApplied, true);
            }, () -> {
                return valueToBeApplied;
            }, () -> {
                return shouldApplyOriginalValue ? valueToBeApplied : convert(type, name, valAndOrigVal.get("origVal"), reflectedValueId(valAndOrigVal, "origVal"), companionFinder);
            }, type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
        } else {
            // in case where no application is needed (unmodified untouched) the value should be validated only
            validateAnd(() -> {
                // do nothing
            }, () -> {
                return shouldApplyOriginalValue
                        ? convert(type, name, valAndOrigVal.get("origVal"), reflectedValueId(valAndOrigVal, "origVal"), companionFinder)
                                : convert(type, name, valAndOrigVal.get("val"), reflectedValueId(valAndOrigVal, "val"), companionFinder);
            }, () -> {
                return convert(type, name, valAndOrigVal.get("origVal"), reflectedValueId(valAndOrigVal, "origVal"), companionFinder);
            }, type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
        }
    }
    
    /**
     * Creates lightweight mock entity instance which will be invalid against {@link EntityExistsValidator} due to empty ID.
     * ToString conversion will give us {@link AbstractEntity#KEY_NOT_ASSIGNED}.
     * <p>
     * This mock instance contains the string query by which the entity was not found.
     * 
     * @param type
     * @param stringQuery -- string query by which the entity was not found
     * 
     * @return
     */
    public static AbstractEntity<?> createMockNotFoundEntity(final Class<?> type, final String stringQuery) {
        if (isEmpty(stringQuery)) {
            throw new EntityResourceUtilsException(format("Mock 'not found' entity could not be created due to empty 'stringQuery' [%s].", stringQuery));
        }
        final AbstractEntity<?> mockEntity = newPlainEntity((Class<AbstractEntity<?>>) type, null);
        mockEntity.set(DESC, stringQuery);
        return mockEntity;
    }
    
    /**
     * Creates a string that can be used for 'not found mock' entity serialisation.
     * 
     * @param stringQuery
     * @return
     */
    public static String createNotFoundMockString(final String stringQuery) {
        return NOT_FOUND_MOCK_PREFIX + stringQuery;
    }
    
    /**
     * Returns indication whether <code>obj</code> represents 'mock not found entity'.
     * 
     * @param obj
     * @return
     */
    public static boolean isMockNotFoundEntity(final Object obj) {
        return obj instanceof AbstractEntity /* obj can be null and will return false as a result */
                && ((AbstractEntity) obj).getId() == null
                && (obj instanceof PropertyDescriptor && ((PropertyDescriptor) obj).getKey() == null || KEY_NOT_ASSIGNED.equals(obj.toString()) )
                && !isEmpty(((AbstractEntity) obj).getDesc());
    }
    
    /**
     * Converts <code>entity</code> to serialisation string.
     * 
     * @param specificConverter -- used to convert entity if it is not 'not found mock', otherwise the standard scheme for 'not found mocks' is used
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> String entityWithMocksToString(final Function<T, String> specificConverter, final T entity) {
        if (isMockNotFoundEntity(entity)) {
            return createNotFoundMockString(entity.get(DESC));
        } else {
            return specificConverter.apply(entity);
        }
    }
    
    /**
     * Converts serialisation <code>str</code> to entity.
     * 
     * @param specificConverter -- used to convert string if it does not represent 'not found mock', otherwise the standard scheme for 'not found mocks' is used
     * @param str
     * @param type
     * @return
     */
    public static <T extends AbstractEntity<?>> T entityWithMocksFromString(final Function<String, T> specificConverter, final String str, final Class<?> type) {
        if (str.startsWith(NOT_FOUND_MOCK_PREFIX)) {
            return (T) createMockNotFoundEntity(type, str.replaceFirst(quote(NOT_FOUND_MOCK_PREFIX), ""));
        }
        return specificConverter.apply(str);
    }
    
    /**
     * Validates the property on subject of conflicts and <code>perform[s]Action</code>.
     *
     * @param performAction -- the action to be performed in case of successful validation
     * @param calculateStaleNewValue -- function to lazily calculate 'staleNewValue' (heavy operation)
     * @param calculateStaleOriginalValue -- function to lazily calculate 'staleOriginalValue' (heavy operation)
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     * @param isCriteriaEntity
     */
    private static <M extends AbstractEntity<?>> void validateAnd(final Runnable performAction, final Supplier<Object> calculateStaleNewValue, final Supplier<Object> calculateStaleOriginalValue, final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale, final boolean isCriteriaEntity) {
        if (!isEntityStale) {
            performAction.run();
        } else {
            final Object staleOriginalValue = calculateStaleOriginalValue.get();
            final Object freshValue = entity.get(name);
            final Object staleNewValue = calculateStaleNewValue.get();
            if (!isCriteriaEntity && EntityUtils.isConflicting(staleNewValue, staleOriginalValue, freshValue)) {
                // 1) are we trying to revert the value to previous stale value to perform "recovery" to actual persisted value? (this is following of 'Please revert property value to resolve conflict' instruction)
                // or 2) has previously touched / untouched property value "recovered" to actual persisted value?
                if (EntityUtils.equalsEx(staleNewValue, staleOriginalValue)) {
                    logger.info(format("Property [%s] has been recently changed by another user for type [%s] to the value [%s]. Original value is [%s].", name, entity.getClass().getSimpleName(), freshValue, staleOriginalValue));
                    entity.getProperty(name).setDomainValidationResult(Result.warning(entity, CONFLICT_WARNING));
                } else {
                    logger.info(format("Property [%s] has been recently changed by another user for type [%s] to the value [%s]. Stale original value is [%s], newValue is [%s]. Please revert property value to resolve conflict.", name, entity.getClass().getSimpleName(), freshValue, staleOriginalValue, staleNewValue));
                    entity.getProperty(name).setDomainValidationResult(new PropertyConflict(entity, CONFLICT_WARNING + " " + format(RESOLVE_CONFLICT_INSTRUCTION, staleOriginalValue == null ? "" : staleOriginalValue)));
                }
            } else {
                performAction.run();
            }
        }
    }

    /**
     * Extracts reflected value ID for 'val' or 'origVal' reflectedValueName if it exists.
     *
     * @param valAndOrigVal
     * @param reflectedValueName
     * @return
     */
    private static Optional<Long> reflectedValueId(final Map<String, Object> valAndOrigVal, final String reflectedValueName) {
        final Object reflectedValueId = valAndOrigVal.get(reflectedValueName + "Id");
        if (reflectedValueId == null) {
            return Optional.empty();
        } else {
            return Optional.of(extractLongValueFrom(reflectedValueId));
        }
    }

    /**
     * Applies the modified (touched / untouched) property value ('val') against the entity.
     *
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     * @param isCriteriaEntity
     */
    private static <M extends AbstractEntity<?>> void applyModifiedPropertyValue(final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale, final boolean isCriteriaEntity) {
        processPropertyValue(true, false, type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
    }

    /**
     * Applies the unmodified (touched) property value ('origVal') against the entity (using 'enforced mutation').
     *
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     * @param isCriteriaEntity
     */
    private static <M extends AbstractEntity<?>> void applyUnmodifiedPropertyValue(final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale, final boolean isCriteriaEntity) {
        processPropertyValue(true, true, type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
    }

    /**
     * Validates the unmodified (untouched) property value for 'changed by other user' warning.
     *
     * @param type
     * @param name
     * @param valAndOrigVal
     * @param entity
     * @param companionFinder
     * @param isEntityStale
     * @param isCriteriaEntity
     */
    private static <M extends AbstractEntity<?>> void validateUnmodifiedPropertyValue(final Class<M> type, final String name, final Map<String, Object> valAndOrigVal, final M entity, final ICompanionObjectFinder companionFinder, final boolean isEntityStale, final boolean isCriteriaEntity) {
        processPropertyValue(false, true, type, name, valAndOrigVal, entity, companionFinder, isEntityStale, isCriteriaEntity);
    }
    
    /**
     * Disregards the 'required' errors for those properties, that were not 'touched' directly by the user (for both criteria and simple entities).
     *
     * @param entity
     * @param touchedProps -- list of 'touched' properties, i.e. those for which editing has occurred during validation lifecycle (maybe returning to original value thus making them unmodified)
     * @return
     */
    public static <M extends AbstractEntity<?>> M disregardUntouchedRequiredProperties(final M entity, final Set<String> touchedProps) {
        // both criteria and simple entities will be affected
        entity.nonProxiedProperties().filter(mp -> mp.isRequired() && !touchedProps.contains(mp.getName())).forEach(mp -> {
            mp.setRequiredValidationResult(successful(entity));
        });
        return entity;
    }
    
    /**
     * Disregards the 'required' errors for those properties, that were provided with some value and then cleared back to empty value during editing of new entity.
     *
     * @param entity
     * @param touchedProps -- list of 'touched' properties, i.e. those for which editing has occurred during validation lifecycle (maybe returning to original value thus making them unmodified)
     * @return
     */
    private static <M extends AbstractEntity<?>> M disregardTouchedRequiredPropertiesWithEmptyValue(final M entity, final Set<String> touchedProps) {
        // both criteria and simple non-persisted (new) entities will be affected
        if (!entity.isPersisted() || EntityQueryCriteria.class.isAssignableFrom(entity.getType())) {
            entity.nonProxiedProperties().filter(mp -> mp.isRequired() && touchedProps.contains(mp.getName()) && mp.getValue() == null).forEach(mp -> {
                mp.setRequiredValidationResult(successful(entity));
            });
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
            entity.nonProxiedProperties().filter(mp -> mp.isRequired()).forEach(mp -> {
                final String prop = mp.getName();
                final CritOnly critOnlyAnnotation = getPropertyAnnotation(CritOnly.class, managedType, prop);
                if (critOnlyAnnotation != null) {
                    mp.setRequiredValidationResult(successful(entity));
                }
            });
        }
    }
    
    /**
     * Determines property type.
     * <p>
     * The exception from standard logic is only for "collection modification func action", where the type of <code>chosenIds</code>, <code>addedIds</code> and <code>removedIds</code> properties
     * is determined from the second type parameter of the func action type. This is required due to the generic nature of those types (see ID_TYPE parameter in {@link AbstractFunctionalEntityForCollectionModification}).
     *
     * @param type
     * @param propertyName
     * @return
     */
    private static Class<?> determinePropertyType(final Class<?> type, final String propertyName) {
        final Class<?> propertyType;
        if (AbstractFunctionalEntityForCollectionModification.class.isAssignableFrom(type) && AbstractFunctionalEntityForCollectionModification.isCollectionOfIds(propertyName)) {
            if (type.getAnnotatedSuperclass() == null) {
                throw Result.failure(new IllegalStateException(format("The AnnotatedSuperclass of functional entity %s (for collection modification) is somehow not defined.", type.getSimpleName())));
            }
            if (!(type.getAnnotatedSuperclass().getType() instanceof ParameterizedType)) {
                throw Result.failure(new IllegalStateException(format("The AnnotatedSuperclass's Type %s of functional entity %s (for collection modification) is somehow not ParameterizedType.", type.getAnnotatedSuperclass().getType(), type.getSimpleName())));
            }
            final ParameterizedType parameterizedEntityType = (ParameterizedType) type.getAnnotatedSuperclass().getType();
            if (parameterizedEntityType.getActualTypeArguments().length != 1 || !(parameterizedEntityType.getActualTypeArguments()[0] instanceof Class)) {
                throw Result.failure(new IllegalStateException(format("The type parameters %s of functional entity %s (for collection modification) is malformed.", Arrays.asList(parameterizedEntityType.getActualTypeArguments()), type.getSimpleName())));
            }
            propertyType = (Class<?>) parameterizedEntityType.getActualTypeArguments()[0];
        } else {
            propertyType = PropertyTypeDeterminator.determinePropertyType(type, propertyName);
        }
        return propertyType;
    }

    /**
     * Converts <code>reflectedValue</code>, which could be a string, a number, a boolean or a null, to a value of appropriate type (the type of the actual property).
     *
     * @param type
     * @param propertyName
     * @param reflectedValue
     * @param reflectedValueId -- in case where the property is entity-typed, this parameter represent an optional ID of the entity-typed value returned from the client application
     *
     * @return
     */
    private static <M extends AbstractEntity<?>> Object convert(final Class<M> type, final String propertyName, final Object reflectedValue, final Optional<Long> reflectedValueId, final ICompanionObjectFinder companionFinder) {
        if (reflectedValue == null) {
            return null;
        }
        final Class<?> propertyType = determinePropertyType(type, propertyName);

        // NOTE: "missing value" for Java entities is also 'null' as for JS entities
        if (EntityUtils.isEntityType(propertyType)) {
            if (PropertyTypeDeterminator.isCollectional(type, propertyName)) {
                throw new UnsupportedOperationException(format("Unsupported conversion to [%s + %s] from reflected value [%s]. Entity-typed collectional properties are not supported.", type.getSimpleName(), propertyName, reflectedValue));
            }

            final Class<AbstractEntity<?>> entityPropertyType = (Class<AbstractEntity<?>>) propertyType;

            if (EntityUtils.isPropertyDescriptor(entityPropertyType)) {
                final Class<AbstractEntity<?>> enclosingEntityType = (Class<AbstractEntity<?>>) AnnotationReflector.getPropertyAnnotation(IsProperty.class, type, propertyName).value();
                return extractPropertyDescriptor((String) reflectedValue, enclosingEntityType).orElse(null);
            } else if (reflectedValueId.isPresent()) {
                logger.debug(format("ID-based restoration of value: type [%s] property [%s] propertyType [%s] id [%s] reflectedValue [%s].", type.getSimpleName(), propertyName, entityPropertyType.getSimpleName(), reflectedValueId.get(), reflectedValue));
                // regardless of whether entityPropertyType is composite or not, the entity should be retrieved by non-empty reflectedValueId that has been arrived from the client application
                final IEntityDao<AbstractEntity<?>> propertyCompanion = companionFinder.find(entityPropertyType, true);
                return propertyCompanion.findById(reflectedValueId.get(), fetchForProperty(companionFinder, type, propertyName).fetchModel());
            } else if (EntityUtils.isCompositeEntity(entityPropertyType)) {
                logger.debug(format("KEY-based restoration of value: type [%s] property [%s] propertyType [%s] id [%s] reflectedValue [%s].", type.getSimpleName(), propertyName, entityPropertyType.getSimpleName(), reflectedValueId, reflectedValue));
                final String compositeKeyAsString = MiscUtilities.prepare(prepSearchStringForCompositeKey(propertyType, entityPropertyType, (String) reflectedValue));
                final EntityResultQueryModel<AbstractEntity<?>> model = select(entityPropertyType).where().prop(KEY).iLike().val(compositeKeyAsString).model();
                final fetch<AbstractEntity<?>> fetchModel = fetchForProperty(companionFinder, type, propertyName).fetchModel();
                final QueryExecutionModel<AbstractEntity<?>, EntityResultQueryModel<AbstractEntity<?>>> qem = from(model).with(fetchModel).model();
                try {
                    final IEntityDao<AbstractEntity<?>> propertyCompanion = companionFinder.<IEntityDao<AbstractEntity<?>>, AbstractEntity<?>> find(entityPropertyType, true);
                    final Object converted = propertyCompanion.getEntity(qem);
                    
                    return orElseFindByKey(converted, propertyCompanion, fetchModel, compositeKeyAsString);
                } catch (final UnexpectedNumberOfReturnedEntities e) {
                    return null;
                }
            } else {
                logger.debug(format("KEY-based restoration of value: type [%s] property [%s] propertyType [%s] id [%s] reflectedValue [%s].", type.getSimpleName(), propertyName, entityPropertyType.getSimpleName(), reflectedValueId, reflectedValue));
                final String[] keys = MiscUtilities.prepare(Arrays.asList((String) reflectedValue));
                final String key;
                if (keys.length > 1) {
                    throw new IllegalArgumentException(format("Value [%s] does not represent a single key value, which is required for coversion to an instance of type [%s].", reflectedValue, entityPropertyType.getName()));
                } else if (keys.length == 0) {
                    key = "";
                } else {
                    key = keys[0];
                }

                final IEntityDao<AbstractEntity<?>> propertyCompanion = companionFinder.find(entityPropertyType, true);
                return propertyCompanion.findByKeyAndFetch(fetchForProperty(companionFinder, type, propertyName).fetchModel(), key);
            }
            // prev implementation => return propertyCompanion.findByKeyAndFetch(getFetchProvider().fetchFor(propertyName).fetchModel(), reflectedValue);
        } else if (PropertyTypeDeterminator.isCollectional(type, propertyName)) {
            final Class<?> collectionType = Finder.findFieldByName(type, propertyName).getType();
            final boolean isSet = Set.class.isAssignableFrom(collectionType);
            final boolean isList = List.class.isAssignableFrom(collectionType);
            final boolean isStringElem = String.class.isAssignableFrom(propertyType);
            final boolean isLongElem = Long.class.isAssignableFrom(propertyType);
            if (!isSet && !isList || !isStringElem && !isLongElem) {
                throw new UnsupportedOperationException(format("Unsupported conversion to [%s@%s] from reflected value [%s] of collectional type [%s] with [%s] elements. Only [Set / List] of [String / Long] elements are supported.", propertyName, type.getSimpleName(), reflectedValue, collectionType.getSimpleName(), propertyType.getSimpleName()));
            }
            final List<Object> list = (ArrayList<Object>) reflectedValue;
            final Stream<Object> stream = list.stream().map(
                item -> item == null ? null :
                    isStringElem ? item.toString() : extractLongValueFrom(item)
            );
            return stream.collect(Collectors.toCollection(isSet ? LinkedHashSet::new : ArrayList::new));
        } else if (PropertyTypeDeterminator.isMap(type, propertyName)) {
            return reflectedValue;
        } else if (EntityUtils.isString(propertyType)) {
            return reflectedValue;
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            return reflectedValue;
        } else if (EntityUtils.isBoolean(propertyType)) {
            return reflectedValue;
        } else if (EntityUtils.isDate(propertyType)) {
            return reflectedValue instanceof Integer ? new Date(((Integer) reflectedValue).longValue()) : new Date((Long) reflectedValue);
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, type, propertyName);
            final CritOnly critOnly = getPropertyAnnotation(CritOnly.class, type, propertyName);
            final Integer propertyScale = isProperty != null && isProperty.scale() >= 0 ? ((int) isProperty.scale())
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
        } else if (Hyperlink.class.isAssignableFrom(propertyType)) {
            final Map<String, Object> map = (Map<String, Object>) reflectedValue;
            final String linkValue = (String) map.get("value");
            return linkValue == null ? null : new Hyperlink(linkValue);
        } else if (Long.class.isAssignableFrom(propertyType)) {
            return extractLongValueFrom(reflectedValue);
        } else {
            throw new UnsupportedOperationException(format("Unsupported conversion to [%s@%s] from reflected value [%s] of type [%s].", propertyName, type.getSimpleName(), reflectedValue, propertyType.getSimpleName()));
        }
    }

    /**
     * Returns {@code converted} is not {@code null}. Otherwise, tries to call {@link IEntityReader#findByKeyAndFetch(fetch, Object...)}.
     * If that call is unsuccessful then {@code null} is returned.
     * <p>
     * The main purpose of this behaviour is to support ad hoc creation of entities with composite keys, similar as for entities with simple keys.
     *
     * @param converted
     * @param propertyCompanion
     * @param fetchModel
     * @param compositeKeyAsString
     * @return
     */
    private static Object orElseFindByKey(final Object converted, final IEntityDao<AbstractEntity<?>> propertyCompanion, final fetch<AbstractEntity<?>> fetchModel, final String compositeKeyAsString) {
        if (converted == null) {
            try {
                return propertyCompanion.findByKeyAndFetch(fetchModel, compositeKeyAsString);
            } catch (final Exception ex) {
                // we can safely ignore any exceptions in this case
            }
        }
        return converted;
    }

    /**
     * Extracts from number-like <code>reflectedValue</code> its {@link Long} representation.
     *
     * @param reflectedValue
     * @return
     */
    private static Long extractLongValueFrom(final Object reflectedValue) {
        if (reflectedValue instanceof Integer) {
            return ((Integer) reflectedValue).longValue();
        } else if (reflectedValue instanceof Long) {
            return (Long) reflectedValue;
        } else if (reflectedValue instanceof BigInteger) {
            return ((BigInteger) reflectedValue).longValue();
        } else {
            throw new IllegalStateException(format("Unknown number type for 'reflectedValue' (%s) - can not convert to Long.", reflectedValue));
        }
    }

    /**
     * This method prepares a search-by string to search for an entity of type {@code propertyType}, which has a composite key.
     * Special processing is required for some specific platform-level entity types such as {@link PropertyDescriptor}:
     * <ul>
     * <li>If one of the composite key members is of type {@link PropertyDescriptor} then the search-by value needs to be modified by converting the provided string representation
     * for property descriptors to the required form.
     * </ul>
     *
     * @param propertyType
     * @param entityPropertyType
     * @param compositeKeyAsString
     * @return
     */
    private static String prepSearchStringForCompositeKey(final Class<?> propertyType, final Class<AbstractEntity<?>> entityPropertyType, final String compositeKeyAsString) {
        // if one or more composite key members are of type ProperyDescriptor then those values need to be converted to a DB aware representation
        // regrettable this process is error prone due to a potential use of the key member separator as part of property titles...
        final List<Field> keyMembers = Finder.getKeyMembers(entityPropertyType);
        final boolean hasPropDescKeyMembers = keyMembers.stream().anyMatch(f -> EntityUtils.isPropertyDescriptor(f.getType()));
        // do we have key members of type PropertyDescriptor
        if (!hasPropDescKeyMembers) {
            return compositeKeyAsString;
        } else {
            final StringBuilder convertedKeyValue = new StringBuilder();
            String keyValues = compositeKeyAsString; // mutable!
            final String keyMemberSeparator = Reflector.getKeyMemberSeparator((Class<? extends AbstractEntity<DynamicEntityKey>>) propertyType);
            for (int index = 0; index < keyMembers.size(); index++) {
                final boolean isLastKeyMember = index == keyMembers.size() - 1;
                final int separatorIndex = isLastKeyMember ? keyValues.length() : keyValues.indexOf(keyMemberSeparator);
                // there must be exactly keyMembers.size() - 1 separators
                // but just in case let's validate the found index
                if (separatorIndex < 0) {
                    throw new IllegalArgumentException(format("Composite key value [%s] must have [%s] separators.", compositeKeyAsString, keyMembers.size() - 1));
                }
                final String value = isLastKeyMember ? keyValues : keyValues.substring(0, separatorIndex);
                keyValues = isLastKeyMember ? "" : keyValues.substring(separatorIndex + 1);

                final Field field = keyMembers.get(index);
                if (EntityUtils.isPropertyDescriptor(field.getType())) {
                    final Class<AbstractEntity<?>> enclosingEntityType = (Class<AbstractEntity<?>>) AnnotationReflector.getPropertyAnnotation(IsProperty.class, entityPropertyType, field.getName()).value();
                    final Optional<PropertyDescriptor<AbstractEntity<?>>> propDesc = extractPropertyDescriptor(value, enclosingEntityType);
                    if (!propDesc.isPresent()) {
                        throw new IllegalArgumentException(format("Could not convert value [%s] to a property descriptor within type [%s].", value, enclosingEntityType.getSimpleName()));
                    }
                    convertedKeyValue.append(propDesc.get().toString());
                } else {
                    convertedKeyValue.append(value);
                }

                if (index < keyMembers.size() - 1) {
                    convertedKeyValue.append(keyMemberSeparator);
                }
            }
            return convertedKeyValue.toString();
        }
    }

    /**
     * Tries to extract a property descriptor from a given string value.
     *
     * @param reflectedValue
     * @param matcher
     * @return
     */
    private static Optional<PropertyDescriptor<AbstractEntity<?>>> extractPropertyDescriptor(final String value, final Class<AbstractEntity<?>> enclosingEntityType) {
        final List<PropertyDescriptor<AbstractEntity<?>>> allPropertyDescriptors = Finder.getPropertyDescriptors(enclosingEntityType);
        final PojoValueMatcher<PropertyDescriptor<AbstractEntity<?>>> matcher = new PojoValueMatcher<>(allPropertyDescriptors, AbstractEntity.KEY, allPropertyDescriptors.size());
        final List<PropertyDescriptor<AbstractEntity<?>>> matchedPropertyDescriptors = matcher.findMatches(value);
        if (matchedPropertyDescriptors.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(matchedPropertyDescriptors.get(0));
    }

    /**
     * Determines the entity type for which criteria entity will be generated.
     *
     * @param miType
     * @return
     */
    public static <T extends AbstractEntity<?>> Class<T> getEntityType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final EntityType entityTypeAnnotation = miType.getAnnotation(EntityType.class);
        if (entityTypeAnnotation == null) {
            throw new IllegalStateException(format("The menu item type [%s] must be annotated with EntityType annotation", miType.getName()));
        }
        return (Class<T>) entityTypeAnnotation.value();
    }

    /**
     * Determines the miType for which criteria entity was generated.
     *
     * @param miType
     * @return
     */
    public static Class<? extends MiWithConfigurationSupport<?>> getMiType(final Class<? extends AbstractEntity<?>> criteriaType) {
        final MiType annotation = AnnotationReflector.getAnnotation(criteriaType, MiType.class);
        if (annotation == null) {
            throw new IllegalStateException(format("The criteria type [%s] should be annotated with MiType annotation.", criteriaType.getName()));
        }
        return annotation.value();
    }

    /**
     * Determines the master type for which criteria entity was generated.
     *
     * @param criteriaType
     * @return
     */
    public static Class<? extends AbstractEntity<?>> getOriginalType(final Class<? extends AbstractEntity<?>> criteriaType) {
        return getEntityType(getMiType(criteriaType));
    }

    /**
     * Determines the property name of the property from which the criteria property was generated. This is only applicable for entity typed properties.
     *
     * @param propertyName
     * @return
     */
    public static String getOriginalPropertyName(final Class<?> criteriaClass, final String propertyName) {
        return CriteriaReflector.getCriteriaProperty(criteriaClass, propertyName);
    }

    /**
     * Determines the managed (in cdtmae) counter-part for master type for which criteria entity was generated.
     *
     * @param criteriaType
     * @param cdtmae
     * @return
     */
    public static Class<?> getOriginalManagedType(final Class<? extends AbstractEntity<?>> criteriaType, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        return cdtmae.getEnhancer().getManagedType(getOriginalType(criteriaType));
    }

    public static String tabs(final int tabCount) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabCount; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
