package ua.com.fielden.platform.web.utils;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.UnexpectedNumberOfReturnedEntities;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.EntityJsonDeserialiser;
import ua.com.fielden.platform.types.*;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSequencedSet;
import static java.util.Locale.getDefault;
import static java.util.Optional.*;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getPropertyDescriptors;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isCollectional;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.decodeOriginalTypeFromCriteriaType;
import static ua.com.fielden.platform.types.RichText.VALIDATION_RESULT;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * This utility class contains the methods that are shared across EntityResource and EntityValidationResource.
 *
 * @author TG Team
 *
 */
public class EntityResourceUtils {
    private static final String WARN_CONFLICT = "This property has been recently changed.";
    public static final String WARN_CENTRE_CONFIG_CONFLICT = "Configuration with this title already exists.";
    public static final String ERR_MORE_THEN_ONE_ENTITY_FOUND = "Please choose a specific value explicitly from a drop-down.";
    private static final String INFO_RESOLVE_CONFLICT_INSTRUCTION = "Please either edit the value back %sto resolve the conflict or cancel all of your changes.";
    /**
     * Used to indicate the start of 'not found mock' serialisation sequence.
     */
    private static final String NOT_FOUND_MOCK_PREFIX = "__________NOT_FOUND__________";
    /**
     * Used to indicate the start of 'more than one mock' serialisation sequence.
     */
    private static final String MORE_THAN_ONE_MOCK_PREFIX = "__________MORE_THAN_ONE__________";
    private static final Logger logger = getLogger(EntityResourceUtils.class);
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
     * Returns {@code true} if the warning does not represent special 'conflicting' warning, {@code false} otherwise.
     */
    public static boolean isNonConflicting(final Warning warning) {
        return !WARN_CONFLICT.equals(warning.getMessage()) && !WARN_CENTRE_CONFIG_CONFLICT.equals(warning.getMessage());
    }

    /**
     * Returns fetch provider for property or, if the property should not be fetched according to default strategy, returns the 'default' property fetch provider with 'keys'
     * (simple and composite) and 'desc' (if 'desc' exists in domain entity).
     */
    private static <V extends AbstractEntity<?>> IFetchProvider<V> fetchForPropertyOrDefault(
            final ICompanionObjectFinder coFinder,
            final Class<? extends AbstractEntity<?>> entityType,
            final String propertyName)
    {
        return fetchForPropertyOrDefault(coFinder.find(entityType).getFetchProvider(), propertyName);
    }

    /**
     * Returns fetch provider for property or, if the property should not be fetched according to default strategy, returns the 'default' property fetch provider with 'keys'
     * (simple composite) and 'desc' (if 'desc' exists in domain entity).
     */
    public static <V extends AbstractEntity<?>> IFetchProvider<V> fetchForPropertyOrDefault(
            final IFetchProvider<? extends AbstractEntity<?>> fetchProvider,
            final String propertyName)
    {
        return fetchProvider.shouldFetch(propertyName)
                ? fetchProvider.fetchFor(propertyName)
                : fetchProvider.with(propertyName).fetchFor(propertyName);
    }

    /**
     * Determines the version that is shipped with {@code modifiedPropertiesHolder}.
     */
    public static long getVersion(final Map<String, Object> modifiedPropertiesHolder) {
        final Object arrivedVersionVal = modifiedPropertiesHolder.get(AbstractEntity.VERSION);
        return ((Integer) arrivedVersionVal).longValue();
    }

    /**
     * Applies the values from {@code modifiedPropertiesHolder} to {@code entity} by converting the values from client-side
     * component-specific form into values that can be assigned to the entity's properties.
     */
    public static <M extends AbstractEntity<?>> M apply(
            final Map<String, Object> modifiedPropertiesHolder,
            final M entity,
            final ICompanionObjectFinder coFinder)
    {
        final Class<M> type = (Class<M>) entity.getType();
        final boolean isEntityStale = entity.getVersion() > getVersion(modifiedPropertiesHolder);
        final boolean isCriteriaEntity = EntityQueryCriteria.class.isAssignableFrom(type);

        final Set<String> touchedProps = unmodifiableSequencedSet(new LinkedHashSet<>((List<String>) modifiedPropertiesHolder.get("@@touchedProps")));

        // iterate through untouched properties first:
        //  (the order of application does not really matter - untouched properties were really applied earlier through some definers, that originate from touched properties)
        modifiedPropertiesHolder.forEach((name, value) -> {
            if (!name.equals(AbstractEntity.ID) && !name.equals(AbstractEntity.VERSION) && !name.startsWith("@") /* custom properties disregarded */ && !touchedProps.contains(name)) {
                final Map<String, Object> valAndOrigVal = (Map<String, Object>) value;
                // The 'modified' properties are marked using the existence of "val" sub-property.
                if (valAndOrigVal.containsKey("val")) { // this is a modified property
                    applyModifiedPropertyValue(type, name, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
                    // logPropertyApplication("   Apply untouched   modified", true, true, type, name, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
                } else { // this is unmodified property
                    // IMPORTANT:
                    // Untouched properties should not be applied, but validation for conflicts should be performed.
                    validateUnmodifiedPropertyValue(type, name, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
                    // logPropertyApplication("Validate untouched unmodified", false, true, type, name, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
                }
            }
        });
        // iterate through touched properties:
        //  (the order of application is strictly the same as was done by the user in Web UI client - the only difference is
        //  such that properties, that were touched twice or more times, will be applied only once)
        for (final String touchedProp : touchedProps) {
            final Map<String, Object> valAndOrigVal = (Map<String, Object>) modifiedPropertiesHolder.get(touchedProp);
            // The 'modified' properties are marked using the existence of "val" sub-property.
            if (valAndOrigVal.containsKey("val")) { // this is a modified property
                applyModifiedPropertyValue(type, touchedProp, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
                // logPropertyApplication("   Apply   touched   modified", true, true, type, touchedProp, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
            } else { // this is unmodified property
                // IMPORTANT:
                // Unlike to the case of untouched properties, all touched properties should be applied,
                //  even unmodified ones.
                // This is necessary in order to mimic the user interaction with the entity (like was in Swing client)
                //  to have the ACE handlers executed for all touched properties.
                applyUnmodifiedPropertyValue(type, touchedProp, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
                // logPropertyApplication("   Apply   touched unmodified", true, true, type, touchedProp, isEntityStale, valAndOrigVal, entity /* insert interested properties here for e.g. [, "propX", "propY", "prop1", "prop2"] */);
            }
        }
        // IMPORTANT: the check for invalid will populate 'required' checks.
        //            It is necessary in case when some property becomes required after the change of other properties.
        entity.isValid();

        disregardCritOnlyRequiredProperties(entity);
        disregardUntouchedRequiredProperties(entity, touchedProps, isCriteriaEntity);
        disregardTouchedRequiredPropertiesWithEmptyValue(entity, touchedProps, isCriteriaEntity);

        return entity;
    }

    /**
     * Logs property application / validation in a table form to easily debug data flow in method 'apply'.
     *
     * @param shortLog  specifies shorter or wider (with 'type' and staleness) view of information
     * @param properties  specifies what properties are interested
     */
    @SuppressWarnings("unused")
    private static <M extends AbstractEntity<?>> void logPropertyApplication(
            final String actionCaption, final boolean apply,
            final boolean shortLog, final Class<M> type,
            final String name, final boolean isEntityStale,
            final Map<String, Object> valAndOrigVal,
            final M entity,
            final String... properties)
    {
        final Set<String> propertiesToLog = new LinkedHashSet<>(Arrays.asList(properties));
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
                    builder.append(format("%8s = %8s (%8s) ", propertyToLog, entity.get(propertyToLog), entity.getProperty(propertyToLog).getFirstFailure()));
                }
            }
            System.out.println(builder.toString()); // use logger instead of sysout if needed
        }
    }

    /**
     * Validates / applies the property value against the entity.
     *
     * @param apply  indicates whether property application should be performed; if {@code false} then only validation will be performed
     * @param applyOriginalValue  indicates whether the 'origVal' should be applied or 'val'
     */
    private static <M extends AbstractEntity<?>> void processPropertyValue(
            final boolean apply, final boolean applyOriginalValue,
            final Class<M> type, final String name,
            final Map<String, Object> valAndOrigVal,
            final M entity,
            final ICompanionObjectFinder coFinder,
            final boolean isEntityStale, final boolean isCriteriaEntity)
    {
        final Optional<String> optActiveProp = ofNullable((String) valAndOrigVal.get("activeProperty"));
        if (apply) {
            // in case where application is necessary (modified touched, modified untouched, unmodified touched) the value (valueToBeApplied) should be checked on existence and then (if successful) it should be applied
            final String valueToBeAppliedName = applyOriginalValue ? "origVal" : "val";
            final Object valToBeApplied = valAndOrigVal.get(valueToBeAppliedName);
            final Object convertedValue = convert(type, name, valToBeApplied, reflectedValueId(valAndOrigVal, valueToBeAppliedName), optActiveProp, coFinder);
            final Object valueToBeApplied;
            if (valToBeApplied != null && convertedValue == null) {
                final Class<?> propType = determinePropertyType(type, name);
                if (isEntityType(propType)) {
                    // here valToBeApplied must be string; look at 'convert' method with 'reflectedValue' parameter always string for entity-typed 'propertyType'
                    valueToBeApplied = createMockNotFoundEntity((Class<AbstractEntity<?>>) propType, (String) valToBeApplied);
                } else {
                    valueToBeApplied = convertedValue;
                }
            } else {
                valueToBeApplied = convertedValue;
            }
            validateAnd(() -> {
                // Value application should be enforced.
                // This is necessary not only for 'touched unmodified' properties (made earlier), but also for 'touched modified' and 'untouched modified' (new logic, 2017-12).
                // This is necessary because without enforcement, property application (with respective definers execution) could be avoided for seemingly 'modified' properties.
                // This is due to the fact that 'modified' property value is always different from original value, but could be equal to the actual value of the property immediately before application.
                // This situation occurs where the property was modified indirectly from definers of other properties in method 'apply'.
                // 'enforce == true' guarantees that property application with validators / definers will always be actioned.
                entity.getProperty(name).setValue(valueToBeApplied, true);
            }, () -> {
                return valueToBeApplied;
            }, () -> {
                return applyOriginalValue ? valueToBeApplied : convert(type, name, valAndOrigVal.get("origVal"), reflectedValueId(valAndOrigVal, "origVal"), optActiveProp, coFinder);
            }, type, name, entity, isEntityStale, isCriteriaEntity);
        } else {
            // in case where no application is needed (unmodified untouched) the value should be validated only
            validateAnd(() -> {
                // do nothing
            }, () -> {
                return applyOriginalValue
                        ? convert(type, name, valAndOrigVal.get("origVal"), reflectedValueId(valAndOrigVal, "origVal"), optActiveProp, coFinder)
                        : convert(type, name, valAndOrigVal.get("val"), reflectedValueId(valAndOrigVal, "val"), optActiveProp, coFinder);
            }, () -> {
                return convert(type, name, valAndOrigVal.get("origVal"), reflectedValueId(valAndOrigVal, "origVal"), optActiveProp, coFinder);
            }, type, name, entity, isEntityStale, isCriteriaEntity);
        }
    }

    /**
     * Creates a lightweight mock entity instance, which does not pass {@link EntityExistsValidator} due to missing ID.
     * Conversion {@code toString} results in {@link AbstractEntity#KEY_NOT_ASSIGNED}.
     * <p>
     * This mock instance contains a search string, which was used to find an entity, but none was found.
     *
     * @param searchString  search string used to find an entity.
     */
    public static AbstractEntity<?> createMockNotFoundEntity(final Class<? extends AbstractEntity> type, final String searchString) {
        if (isEmpty(searchString)) {
            throw new EntityResourceUtilsException("Mock [not found] entity could not be created due to empty [searchString].");
        }
        if (isUnionEntityType(type)) {
            final List<Field> unionProps = unionProperties((Class<AbstractUnionEntity>) type);
            final Class<? extends AbstractEntity<?>> unionPropType = (Class<? extends AbstractEntity<?>>) unionProps.getFirst().getType();
            final AbstractEntity<?> unionPropValue = newPlainEntity(mock(unionPropType), null).set(DESC, searchString);
            return newPlainEntity(mock(type), null).set(unionProps.getFirst().getName(), unionPropValue);
        }
        return newPlainEntity(mock(type), null).set(DESC, searchString);
    }

    /**
     * Creates a string that can be used for 'not found mock' entity serialisation.
     */
    public static String createNotFoundMockString(final String searchString) {
        return NOT_FOUND_MOCK_PREFIX + searchString;
    }

    /**
     * Creates a string that can be used for 'more than one mock' entity serialisation.
     */
    public static String createMoreThanOneMockString(final String searchString) {
        return MORE_THAN_ONE_MOCK_PREFIX + searchString;
    }

    /**
     * Tests whether an object represents 'mock not found entity'.
     */
    public static boolean isMockNotFoundEntity(final Object obj) {
        return obj instanceof AbstractEntity && MockNotFoundEntityMaker.isMockNotFoundValue((AbstractEntity<?>)obj);
    }

    /**
     * Converts an entity to a serialisation string.
     *
     * @param specificConverter  used to convert an entity if it is not 'not found mock', otherwise the standard scheme for 'not found mocks' is used
     */
    public static <T extends AbstractEntity<?>> String entityWithMocksToString(final Function<T, String> specificConverter, final T entity) {
        if (isMockNotFoundEntity(entity)) {
            // entity.get(DESC) returns the actually typed by user string; property "desc" is used for this purpose, because it is of type String for any entity that may need to be mocked
            // FIXME It would have been better to remove the use of "desc" in favour of a separate, specifically generated property to hold values typed by users (https://github.com/fieldenms/tg/issues/1933).
            //       Property "desc" will be removed from AbstractEntity at some stage in the future.
            return getErrorMessage(entity).isPresent() ? createMoreThanOneMockString(entity.get(DESC)) : createNotFoundMockString(entity.get(DESC));
        } else {
            return specificConverter.apply(entity);
        }
    }

    /**
     * Converts a serialised string to an entity.
     *
     * @param str  serialiased string
     * @param specificConverter  used to convert {@code str} if it does not represent 'not found mock', otherwise the standard scheme for 'not found mocks' is used
     */
    public static <T extends AbstractEntity<?>> T entityWithMocksFromString(final Function<String, T> specificConverter, final String str, final Class<? extends AbstractEntity> type) {
        if (str.startsWith(NOT_FOUND_MOCK_PREFIX)) {
            return (T) createMockNotFoundEntity(type, str.replaceFirst(quote(NOT_FOUND_MOCK_PREFIX), ""));
        } else if (str.startsWith(MORE_THAN_ONE_MOCK_PREFIX)) {
            return (T) createMockFoundMoreThanOneEntity(type, str.replaceFirst(quote(MORE_THAN_ONE_MOCK_PREFIX), ""));
        }
        return specificConverter.apply(str);
    }

    /**
     * Validates the property on subject of conflicts and {@code perform[s]Action}.
     *
     * @param performAction  the action to be performed in case of successful validation
     * @param calculateStaleNewValue  function to lazily calculate 'staleNewValue' (heavy operation)
     * @param calculateStaleOriginalValue  function to lazily calculate 'staleOriginalValue' (heavy operation)
     */
    private static <M extends AbstractEntity<?>> void validateAnd(
            final Runnable performAction,
            final Supplier<Object> calculateStaleNewValue,
            final Supplier<Object> calculateStaleOriginalValue,
            final Class<M> type, final String name,
            final M entity,
            final boolean isEntityStale, final boolean isCriteriaEntity)
    {
        if (!isEntityStale) {
            performAction.run();
        } else {
            final Object staleOriginalValue = calculateStaleOriginalValue.get();
            final Object rawFreshValue = entity.get(name);
            // In case of non-null (instanceof covers this) entity-typed collectional property ...
            final Object freshValue = rawFreshValue instanceof Collection<?> freshCollection && isEntityType(determinePropertyType(type, name))
                // ... convert to a simplified List<String> to conform with 'staleOriginalValue' / 'staleNewValue'.
                // See 'tg-entity-binder-behavior._extractModifiedPropertiesHolder.convert' function for more details.
                // Also allow 'null' values because there are no restrictions on them.
                ? freshCollection.stream().map(ent -> Objects.toString(ent, null)).toList()
                : rawFreshValue;
            final Object staleNewValue = calculateStaleNewValue.get();
            if (!isCriteriaEntity && isConflicting(staleNewValue, staleOriginalValue, freshValue)) {
                // 1) are we trying to revert the value to previous stale value to perform "recovery" to actual persisted value? (this is following of 'Please revert property value to resolve conflict' instruction)
                // or 2) has previously touched / untouched property value "recovered" to actual persisted value?
                if (equalsEx(staleNewValue, staleOriginalValue)) {
                    logger.info(format("Property [%s] has been recently changed by another user for type [%s] to the value [%s]. Original value is [%s].", name, entity.getClass().getSimpleName(), freshValue, staleOriginalValue));
                    entity.getProperty(name).setDomainValidationResult(Result.warning(entity, WARN_CONFLICT));
                } else {
                    logger.info(format("Property [%s] has been recently changed by another user for type [%s] to the value [%s]. Stale original value is [%s], newValue is [%s]. Please revert property value to resolve conflict.", name, entity.getClass().getSimpleName(), freshValue, staleOriginalValue, staleNewValue));
                    entity.getProperty(name).setDomainValidationResult(new PropertyConflict(
                        entity,
                        WARN_CONFLICT + " " + INFO_RESOLVE_CONFLICT_INSTRUCTION.formatted(
                            staleOriginalValue instanceof RichText ? ""
                            : "to [%s] ".formatted(Objects.toString(staleOriginalValue, ""))
                        )
                    ));
                }
            } else {
                performAction.run();
            }
        }
    }

    /**
     * Extracts reflected value ID for 'val' or 'origVal' reflectedValueName if it exists.
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
     */
    private static <M extends AbstractEntity<?>> void applyModifiedPropertyValue(
            final Class<M> type, final String name,
            final Map<String, Object> valAndOrigVal,
            final M entity,
            final ICompanionObjectFinder coFinder,
            final boolean isEntityStale,
            final boolean isCriteriaEntity)
    {
        processPropertyValue(true, false, type, name, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
    }

    /**
     * Applies the unmodified (touched) property value ('origVal') against the entity (using 'enforced mutation').
     */
    private static <M extends AbstractEntity<?>> void applyUnmodifiedPropertyValue(
            final Class<M> type, final String name,
            final Map<String, Object> valAndOrigVal,
            final M entity,
            final ICompanionObjectFinder coFinder,
            final boolean isEntityStale,
            final boolean isCriteriaEntity)
    {
        processPropertyValue(true, true, type, name, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
    }

    /**
     * Validates the unmodified (untouched) property value for 'changed by another user' warning.
     */
    private static <M extends AbstractEntity<?>> void validateUnmodifiedPropertyValue(
            final Class<M> type, final String name,
            final Map<String, Object> valAndOrigVal,
            final M entity,
            final ICompanionObjectFinder coFinder,
            final boolean isEntityStale,
            final boolean isCriteriaEntity)
    {
        processPropertyValue(false, true, type, name, valAndOrigVal, entity, coFinder, isEntityStale, isCriteriaEntity);
    }

    /**
     * Disregards the 'required' errors for those properties, that were not 'touched' directly by the user (for both criteria and simple entities).
     *
     * @param touchedProps  properties for which editing has occurred during validation lifecycle (maybe returning to original value thus making them unmodified)
     */
    public static <M extends AbstractEntity<?>> M disregardUntouchedRequiredProperties(
            final M entity,
            final Set<String> touchedProps,
            final boolean isCriteriaEntity)
    {
        // both criteria and simple entities will be affected
        entity.nonProxiedProperties().filter(mp -> mp.isRequired() && !touchedProps.contains(mp.getName())).forEach(mp -> {
            mp.setRequiredValidationResult(successful(entity));
            // stale validation error for other validator may remain on the property;
            // this is possible if the property is dependent on other and that other property makes this property required;
            // before it was made required, the property may have been in error in other validator, and the entity at the time may have been not constructed fully (the property validation state being stale);
            // need to revalidate without requiredness validation
            if (!isCriteriaEntity && !mp.isValid()) { // only do this for Entity Master entities and trigger revalidation only for erroneous (after req error clearing) properties
                mp.revalidate(true);
            }
        });
        return entity;
    }

    /**
     * Disregards the 'required' errors for those properties, that were provided with some value and then cleared back to empty value during editing of new entity.
     *
     * @param touchedProps properties for which editing has occurred during validation lifecycle (maybe returning to original value thus making them unmodified)
     */
    private static <M extends AbstractEntity<?>> M disregardTouchedRequiredPropertiesWithEmptyValue(
            final M entity,
            final Set<String> touchedProps,
            final boolean isCriteriaEntity)
    {
        // both criteria and simple non-persisted (new) entities will be affected
        if (!entity.isPersisted() || isCriteriaEntity) {
            entity.nonProxiedProperties().filter(mp -> mp.isRequired() && touchedProps.contains(mp.getName()) && mp.getValue() == null).forEach(mp -> {
                mp.setRequiredValidationResult(successful(entity));
                // stale validation error for other validator may remain on the property;
                // this is possible if the property is dependent on other and that other property makes this property required;
                // before it was made required, the property may have been in error in other validator, and the entity at the time may have been not constructed fully (the property validation state being stale);
                // need to revalidate without requiredness validation
                if (!isCriteriaEntity && !mp.isValid()) { // only do this for Entity Master entities and trigger revalidation only for erroneous (after req error clearing) properties
                    mp.revalidate(true);
                }
            });
        }
        return entity;
    }

    /**
     * Disregards the 'required' errors for crit-only properties on masters for non-criteria entity types.
     */
    public static <M extends AbstractEntity<?>> void disregardCritOnlyRequiredProperties(final M entity) {
        final Class<?> managedType = entity.getType();
        if (!EntityQueryCriteria.class.isAssignableFrom(managedType)) {
            entity.nonProxiedProperties().filter(MetaProperty::isRequired).forEach(mp -> {
                final String prop = mp.getName();
                final CritOnly critOnlyAnnotation = getPropertyAnnotation(CritOnly.class, managedType, prop);
                if (critOnlyAnnotation != null) {
                    mp.setRequiredValidationResult(successful(entity));
                }
            });
        }
    }

    /**
     * Determines a property type.
     * <p>
     * The exception from standard logic is only for "collection modification func action", where the type of <code>chosenIds</code>, <code>addedIds</code> and <code>removedIds</code> properties
     * is determined from the second type parameter of the func action type. This is required due to the generic nature of those types (see ID_TYPE parameter in {@link AbstractFunctionalEntityForCollectionModification}).
     */
    private static Class<?> determinePropertyType(final Class<?> type, final String propertyName) {
        final Class<?> propertyType;
        if (AbstractFunctionalEntityForCollectionModification.class.isAssignableFrom(type) && AbstractFunctionalEntityForCollectionModification.isCollectionOfIds(propertyName)) {
            if (type.getAnnotatedSuperclass() == null) {
                throw failure(new IllegalStateException(format("The AnnotatedSuperclass of functional entity %s (for collection modification) is somehow not defined.", type.getSimpleName())));
            }
            if (!(type.getAnnotatedSuperclass().getType() instanceof ParameterizedType parameterizedEntityType)) {
                throw failure(new IllegalStateException(format("The AnnotatedSuperclass's Type %s of functional entity %s (for collection modification) is somehow not ParameterizedType.", type.getAnnotatedSuperclass().getType(), type.getSimpleName())));
            }
            if (parameterizedEntityType.getActualTypeArguments().length != 1 || !(parameterizedEntityType.getActualTypeArguments()[0] instanceof Class)) {
                throw failure(new IllegalStateException(format("The type parameters %s of functional entity %s (for collection modification) is malformed.", Arrays.asList(parameterizedEntityType.getActualTypeArguments()), type.getSimpleName())));
            }
            propertyType = (Class<?>) parameterizedEntityType.getActualTypeArguments()[0];
        } else {
            propertyType = PropertyTypeDeterminator.determinePropertyType(type, propertyName);
        }
        return propertyType;
    }

    /**
     * Converts a raw reflected value to a value that matches the property's type.
     *
     * @param type  type that owns the property
     * @param reflectedValue  raw reflected value to be converted
     * @param reflectedValueId  if a property is entity-typed, represent an ID of the entity-typed value returned from the client application
     * @param optActiveProp  if a property has a union entity type, represents the active property's name in the entity-typed value
     */
    private static <M extends AbstractEntity<?>> Object convert(
            final Class<M> type, final String propertyName,
            final Object reflectedValue, final Optional<Long> reflectedValueId,
            final Optional<String> optActiveProp,
            final ICompanionObjectFinder companionFinder)
    {
        if (reflectedValue == null) {
            return null;
        }
        final Class<?> propertyType = determinePropertyType(type, propertyName);

        // NOTE: "missing value" for Java entities is also 'null' as for JS entities
        if (isEntityType(propertyType)) {
            if (isCollectional(type, propertyName)) {
                return reflectedValue;
            }

            final Class<AbstractEntity<?>> entityPropertyType = (Class<AbstractEntity<?>>) propertyType;

            final String reflectedValueAsString = (String) reflectedValue;
            if (EntityUtils.isPropertyDescriptor(entityPropertyType)) {
                final Class<AbstractEntity<?>> enclosingEntityType = (Class<AbstractEntity<?>>) getPropertyAnnotation(IsProperty.class, type, propertyName).value();
                return extractPropertyDescriptor(reflectedValueAsString, enclosingEntityType).orElse(null);
            } else {
                final fetch<AbstractEntity<?>> fetch = fetchForProperty(companionFinder, type, propertyName).fetchModel();
                final IEntityDao<AbstractEntity<?>> propertyCompanion = companionFinder.<IEntityDao<AbstractEntity<?>>, AbstractEntity<?>> find(entityPropertyType, !isUnionEntityType(entityPropertyType));
                if (reflectedValueId.isPresent()) {
                    logger.debug(format("ID-based restoration of value: type [%s] property [%s] propertyType [%s] id [%s] reflectedValue [%s].", type.getSimpleName(), propertyName, entityPropertyType.getSimpleName(), reflectedValueId.get(), reflectedValue));
                    // regardless of whether entityPropertyType is composite or not, the entity should be retrieved by non-empty reflectedValueId that has been arrived from the client application
                    return propertyCompanion.findById(true, reflectedValueId.get(), fetch);
                } else {
                    return findAndFetchBy(reflectedValueAsString, entityPropertyType, optActiveProp, fetch, propertyCompanion);
                }
            }
            // prev implementation => return propertyCompanion.findByKeyAndFetch(getFetchProvider().fetchFor(propertyName).fetchModel(), reflectedValue);
        } else if (isCollectional(type, propertyName)) {
            final Class<?> collectionType = Finder.findFieldByName(type, propertyName).getType();
            final boolean isSet = Set.class.isAssignableFrom(collectionType);
            final boolean isList = List.class.isAssignableFrom(collectionType);
            final boolean isString = String.class.isAssignableFrom(propertyType);
            final boolean isLong = Long.class.isAssignableFrom(propertyType);
            if (!isSet && !isList || !isString && !isLong) {
                throw new UnsupportedOperationException(format("Unsupported conversion to [%s@%s] from reflected value [%s] of collectional type [%s] with [%s] elements. Only [Set / List] of [String / Long] elements are supported.", propertyName, type.getSimpleName(), reflectedValue, collectionType.getSimpleName(), propertyType.getSimpleName()));
            }
            final List<Object> list = (ArrayList<Object>) reflectedValue;
            final Stream<Object> stream = list.stream()
                    .map(item -> item == null ? null : (isString ? item.toString() : extractLongValueFrom(item)));
            return stream.collect(Collectors.toCollection(isSet ? LinkedHashSet::new : ArrayList::new));
        } else if (PropertyTypeDeterminator.isMap(type, propertyName)) {
            return reflectedValue;
        } else if (EntityUtils.isString(propertyType)) {
            return reflectedValue;
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            if (reflectedValue instanceof Long || reflectedValue instanceof BigInteger) {
                throw new IllegalStateException("Reflected value %s (%s) is too large for property [%s.%s : %s]".formatted(
                        reflectedValue, reflectedValue.getClass().getTypeName(), type.getSimpleName(), propertyName, propertyType.getTypeName()));
            }
            return reflectedValue;
        } else if (EntityUtils.isBoolean(propertyType)) {
            return reflectedValue;
        } else if (EntityUtils.isDate(propertyType)) {
            return reflectedValue instanceof Integer i ? new Date((i).longValue()) : new Date((Long) reflectedValue);
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, type, propertyName);
            final CritOnly critOnly = getPropertyAnnotation(CritOnly.class, type, propertyName);
            final int propertyScale = isProperty != null && isProperty.scale() >= 0
                    ? isProperty.scale()
                    : (critOnly != null && critOnly.scale() >= 0 ? ((int) critOnly.scale()) : 2)/* default value from Hibernate */;

            return switch (reflectedValue) {
                case Integer i -> new BigDecimal(i).setScale(propertyScale, RoundingMode.HALF_UP);
                case Long l -> BigDecimal.valueOf(l).setScale(propertyScale, RoundingMode.HALF_UP);
                case BigInteger bigInteger -> new BigDecimal(bigInteger).setScale(propertyScale, RoundingMode.HALF_UP);
                case BigDecimal bigDecimal -> bigDecimal.setScale(propertyScale, RoundingMode.HALF_UP);
                default -> throw new IllegalStateException("Unknown number type for 'reflectedValue': %s.".formatted(reflectedValue.getClass().getTypeName()));
            };
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
        } else if (RichText.class.isAssignableFrom(propertyType)){
            final Map<String, Object> map = (Map<String, Object>) reflectedValue;

            final @Nullable Result validationResult = convertRichTextValidationResult(map.get(VALIDATION_RESULT));
            if (validationResult != null) {
                return RichText.fromUnsuccessfulValidationResult(validationResult);
            }
            else {
                // in a modified RichText value we care only about formatted text
                final String formattedText = (String) map.get(RichText.FORMATTED_TEXT);
                return formattedText == null ? null : RichText.fromHtml(formattedText);
            }
        } else if (Long.class.isAssignableFrom(propertyType)) {
            return extractLongValueFrom(reflectedValue);
        } else if (Class.class.isAssignableFrom(propertyType)) {
            try {
                return ClassesRetriever.findClass((String) reflectedValue); // full class names for already registered server-side Class'es are supported
            } catch (final Exception ex) {
                throw new EntityResourceUtilsException(format("Conversion to [%s@%s] from reflected value [%s] of type [%s] failed.", propertyName, type.getSimpleName(), reflectedValue, propertyType.getSimpleName()), ex);
            }
        } else {
            throw new UnsupportedOperationException(format("Unsupported conversion to [%s@%s] from reflected value [%s] of type [%s].", propertyName, type.getSimpleName(), reflectedValue, propertyType.getSimpleName()));
        }
    }

    /**
     * Converts the specified object to a validation result for {@link RichText.Invalid}.
     * If the object is {@code null}, returns {@code null}.
     * Otherwise, the object must represent an unsuccessful validation result.
     *
     * @return  unsuccessful validation result or {@code null}
     *
     * @see RichTextJsonDeserialiser
     */
    private static @Nullable Result convertRichTextValidationResult(final @Nullable Object object) {
        if (object == null) {
            return null;
        }
        else if (object instanceof Map rawMap) {
            final var map = (Map<String, Object>) rawMap;
            final var message = (String) map.get(Result.MESSAGE);
            if (message == null) {
                throw new EntityResourceUtilsException("Message is required for RichText validaton result.");
            }
            return failure(map.get(Result.INSTANCE), message);
        }
        else {
            throw new EntityResourceUtilsException(
                    format("Conversion error: expected [%s.%s] with type [%s], but was [%s].",
                           RichText.class.getSimpleName(), RichText.FORMATTED_TEXT,
                           Map.class.getTypeName(),
                           object.getClass().getTypeName()));
        }
    }

    /**
     * Finds entity value by <code>searchString</code> from autocompleter.
     * <p>
     * This method takes care of proper composite entity search string decomposition (including situations with {@link PropertyDescriptor} as key member) and early checking for search string correctness.
     *
     * @param entityType  the type of entity being looked for
     * @param fetch  fetch model for resultant entity
     * @param companion  companion for the entity being looked for
     */
    public static AbstractEntity<?> findAndFetchBy(
            final String searchString, final Class<AbstractEntity<?>> entityType,
            final Optional<String> optActiveProp, final fetch<AbstractEntity<?>> fetch,
            final IEntityDao<AbstractEntity<?>> companion)
    {
        if (isCompositeEntity(entityType)) {
            //logger.debug(format("KEY-based restoration of value: type [%s] property [%s] propertyType [%s] id [%s] reflectedValue [%s].", type.getSimpleName(), propertyName, entityPropertyType.getSimpleName(), reflectedValueId, reflectedValue));
            final String compositeKeyAsString = MiscUtilities.prepare(prepSearchStringForCompositeKey(entityType, searchString));
            final var qem = from(
                    select(entityType).where().prop(KEY).iLike().val(compositeKeyAsString).model().setFilterable(true))
                    .with(fetch).model();
            try {
                final AbstractEntity<?> converted = companion.getEntity(qem);
                return orElseFindByKey(converted, companion, fetch, compositeKeyAsString);
            } catch (final UnexpectedNumberOfReturnedEntities e) {
                return null;
            }
        } else if (isUnionEntityType(entityType)) {
            final var entities = companion.getFirstEntities(
                from(
                    select(entityType).where()
                    .prop(
                        // using a condition for an active property, if it is present, guarantees a narrower search result in case of duplicate key values among entity types of union properties
                        optActiveProp.map(activeProp -> activeProp + "." + KEY).orElse(KEY)
                    ).iLike().val(searchString)
                    .model()
                    .setFilterable(true)
                )
                .with(fetch)
                .model(),
                2
            );
            if (entities.isEmpty()) {
                return null;
            } else if (entities.size() == 1) {
                return entities.getFirst();
            } else {
                // there can be many associated entities for unions without concrete active prop (same keys trough different subtypes);
                // otherwise, return empty entity similarly as we do for composite ones (see UnexpectedNumberOfReturnedEntities exception)
                return optActiveProp.isPresent() ? null : createMockFoundMoreThanOneEntity(entityType, searchString);
            }
        } else {
            //logger.debug(format("KEY-based restoration of value: type [%s] property [%s] propertyType [%s] id [%s] reflectedValue [%s].", type.getSimpleName(), propertyName, entityPropertyType.getSimpleName(), reflectedValueId, reflectedValue));
            final String[] keys = MiscUtilities.prepare(List.of(searchString));
            final String key;
            if (keys.length > 1) {
                throw new IllegalArgumentException(format("Value [%s] does not represent a single key value, which is required for coversion to an instance of type [%s].", searchString, entityType.getName()));
            } else if (keys.length == 0) {
                key = "";
            } else {
                key = keys[0];
            }
            return companion.findByKeyAndFetch(true, fetch, key);
        }
    }

    /**
     * The same as {@link #createMockNotFoundEntity(Class, String)}, but with a specific error message assigned to field {@code PROP_NAME_HOLDING_ERROR_MSG}.
     *
     * @param searchString  search string used to find an entity.
     */
    public static AbstractEntity<?> createMockFoundMoreThanOneEntity(final Class<? extends AbstractEntity> type, final String searchString) {
        return setErrorMessage(createMockNotFoundEntity(type, searchString), ERR_MORE_THEN_ONE_ENTITY_FOUND);
    }

    /**
     * Returns {@code converted} is not {@code null}. Otherwise, tries to call {@link IEntityReader#findByKeyAndFetch(fetch, Object...)}.
     * If that call is unsuccessful then {@code null} is returned.
     * <p>
     * The main purpose of this behaviour is to support ad hoc creation of entities with composite keys, similar as for entities with simple keys.
     */
    private static AbstractEntity<?> orElseFindByKey(
            final AbstractEntity<?> converted,
            final IEntityDao<AbstractEntity<?>> propertyCompanion,
            final fetch<AbstractEntity<?>> fetchModel,
            final String compositeKeyAsString)
    {
        if (converted == null) {
            try {
                return propertyCompanion.findByKeyAndFetch(true, fetchModel, compositeKeyAsString);
            } catch (final Exception ex) {
                // we can safely ignore any exceptions in this case
            }
        }
        return converted;
    }

    /**
     * Extracts from number-like <code>reflectedValue</code> its {@link Long} representation.
     */
    private static Long extractLongValueFrom(final Object reflectedValue) {
        return switch (reflectedValue) {
            case Integer i -> i.longValue();
            case Long l -> l;
            case BigInteger bigInteger -> bigInteger.longValue();
            case null, default -> throw new IllegalStateException(format("Number %s cannot be converted to Long.", reflectedValue));
        };
    }

    /**
     * This method prepares a search-by string to search for an entity of type {@code propertyType}, which has a composite key.
     * Special processing is required for some specific platform-level entity types such as {@link PropertyDescriptor}:
     * <ul>
     * <li>If one of the composite key members is of type {@link PropertyDescriptor} then the search-by value needs to be modified by converting the provided string representation
     * for property descriptors to the required form.
     * </ul>
     */
    private static String prepSearchStringForCompositeKey(final Class<AbstractEntity<?>> entityPropertyType, final String compositeKeyAsString) {
        // If one or more composite key members are of type PropertyDescriptor then those values need to be converted to a DB-aware representation.
        // Regrettable this process is error-prone due to a potential use of the key member separator as part of property titles...
        final List<Field> keyMembers = Finder.getKeyMembers(entityPropertyType);
        final boolean hasPropDescKeyMembers = keyMembers.stream().anyMatch(f -> EntityUtils.isPropertyDescriptor(f.getType()));
        // Do we have key members of type PropertyDescriptor?
        if (!hasPropDescKeyMembers) {
            return compositeKeyAsString;
        } else {
            final StringBuilder convertedKeyValue = new StringBuilder();
            String keyValues = compositeKeyAsString; // mutable!
            final Class<?> propertyType = entityPropertyType;
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
                    final Class<AbstractEntity<?>> enclosingEntityType = (Class<AbstractEntity<?>>) getPropertyAnnotation(IsProperty.class, entityPropertyType, field.getName()).value();
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
     */
    private static Optional<PropertyDescriptor<AbstractEntity<?>>> extractPropertyDescriptor(final String value, final Class<AbstractEntity<?>> enclosingEntityType) {
        final var allPropertyDescriptors = getPropertyDescriptors(enclosingEntityType);
        final var matchedPropertyDescriptors = new PojoValueMatcher<>(allPropertyDescriptors, KEY, allPropertyDescriptors.size()).findMatches(value);
        if (matchedPropertyDescriptors.size() != 1) {
            return empty();
        }
        return of(matchedPropertyDescriptors.getFirst());
    }

    /**
     * Determines the entity type for which criteria entity will be generated.
     */
    public static <T extends AbstractEntity<?>> Class<T> getEntityType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final EntityType entityTypeAnnotation = miType.getAnnotation(EntityType.class);
        if (entityTypeAnnotation == null) {
            throw new IllegalStateException(format("Menu item type [%s] must be annotated with @EntityType.", miType.getTypeName()));
        }
        return (Class<T>) entityTypeAnnotation.value();
    }

    /**
     * Determines the master type for which criteria entity was generated.
     */
    public static Class<? extends AbstractEntity<?>> getOriginalType(final Class<? extends AbstractEntity<?>> criteriaType) {
        return (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(decodeOriginalTypeFromCriteriaType(stripIfNeeded(criteriaType).getName()));
    }

    /**
     * Determines the property name of the property from which the criteria property was generated. This is only applicable for entity typed properties.
     */
    public static String getOriginalPropertyName(final Class<?> criteriaClass, final String propertyName) {
        return CriteriaReflector.getCriteriaProperty(criteriaClass, propertyName);
    }

    /**
     * Determines the managed (in cdtmae) counter-part for master type for which criteria entity was generated.
     */
    public static Class<?> getOriginalManagedType(final Class<? extends AbstractEntity<?>> criteriaType, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        return cdtmae.getEnhancer().getManagedType(getOriginalType(criteriaType));
    }

    public static String tabs(final int tabCount) {
        return "  ".repeat(Math.max(0, tabCount));
    }

}
