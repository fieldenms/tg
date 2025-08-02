package ua.com.fielden.platform.entity.meta;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.mutator.*;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.PropertyBceOrAceDefinitionException;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.*;
import ua.com.fielden.platform.entity.validation.annotation.*;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.StringConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;

/**
 * Base implementation for {@link IMetaPropertyFactory}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractMetaPropertyFactory implements IMetaPropertyFactory {

    public static final String ERR_UNSUPPORTED_VALIDATION_ANNOTATION = "Unsupported validation annotation has been encountered.";
    public static final String ERR_UNRECOGNISED_VALIDATION_ANNOTATION = "Unrecognised validation annotation has been encountered.";
    public static final String ERR_INJECTOR_IS_MISSING = "Meta-property factory is not fully initialised -- injector is missing";
    public static final String ERR_HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER = "BCE/ACE handlers should not have a another BCE/ACE handler as its parameter.";
    public static final String ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM = "Invalid property name [%s] for entity [%s].";

    protected final FinalValidator[] notPersistedOnlyFinalValidator = new FinalValidator[]{new FinalValidator(false, false)};
    protected final FinalValidator[] notPersistedOnlyAndNullIsValueFinalValidator = new FinalValidator[]{new FinalValidator(false, true)};
    protected final FinalValidator[] persistedOnlyFinalValidator = new FinalValidator[]{new FinalValidator(true, false)};
    protected final FinalValidator[] persistedOnlyAndNullIsValueFinalValidator = new FinalValidator[]{new FinalValidator(true, true)};
    protected final Map<Integer, GreaterOrEqualValidator> greaterOrEqualsValidators = new ConcurrentHashMap<>();
    protected final Map<Integer, MaxLengthValidator> maxLengthValidators = new ConcurrentHashMap<>();
    protected final Map<Integer, MaxValueValidator> maxValueValidators = new ConcurrentHashMap<>();
    protected final Map<Class<?>, Map<String, GePropertyValidator<?>>> geRangeValidators = new ConcurrentHashMap<>();
    protected final Map<Class<?>, Map<String, LePropertyValidator<?>>> leRangeValidators = new ConcurrentHashMap<>();
    // type, property, array of handlers
    protected final Map<Class<?>, Map<String, IBeforeChangeEventHandler<?>[]>> beforeChangeEventHandlers = new ConcurrentHashMap<>();
    protected final Map<Class<?>, Map<String, IAfterChangeEventHandler<?>>> afterChangeEventHandlers = new ConcurrentHashMap<>();

    // *** INJECTABLE FIELDS
    private Injector injector;
    protected DomainValidationConfig domainValidationConfig;
    protected DomainMetaPropertyConfig domainMetaConfig;
    private IDates dates;
    // ***

    protected AbstractMetaPropertyFactory() {}

    @Inject
    void setDomainValidationConfig(final DomainValidationConfig domainValidationConfig) {
        this.domainValidationConfig = domainValidationConfig;
    }

    @Inject
    void setDomainMetaConfig(final DomainMetaPropertyConfig domainMetaConfig) {
        this.domainMetaConfig = domainMetaConfig;
    }

    @Inject
    void setDates(final IDates dates) {
        this.dates = dates;
    }

    @Inject
    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public IBeforeChangeEventHandler<?>[] create(//
            final Annotation annotation,//
            final AbstractEntity<?> entity,//
            final String propertyName,//
            final Class<?> propertyType) throws Exception {
        if (injector == null) {
            throw new IllegalStateException(ERR_INJECTOR_IS_MISSING);
        }
        // identify the type of annotation
        ValidationAnnotation value = null;
        for (final ValidationAnnotation validationAnnotation : ValidationAnnotation.values()) {
            if (validationAnnotation.getType().equals(annotation.annotationType())) {
                value = validationAnnotation;
                break;
            }
        }
        // check whether it can be recognised as a valid annotation permitted for validation purpose
        if (value == null) {
            throw new EntityDefinitionException(ERR_UNRECOGNISED_VALIDATION_ANNOTATION);
        }
        // try to instantiate validator
        return switch (value) {
            case ENTITY_EXISTS -> new IBeforeChangeEventHandler[] { createEntityExists((EntityExists) annotation) };
            case FINAL -> createFinalValidator(entity, propertyName, (Final) annotation);
            case GREATER_OR_EQUAL -> new IBeforeChangeEventHandler[] { createGreaterOrEqualValidator(((GreaterOrEqual) annotation).value()) };
            case LE_PROPETY -> new IBeforeChangeEventHandler[] { createLePropertyValidator(entity, propertyName, propertyType, ((LeProperty) annotation).value()) };
            case GE_PROPETY -> new IBeforeChangeEventHandler[] { createGePropertyValidator(entity, propertyName, propertyType, ((GeProperty) annotation).value()) };
            case MAX -> {
                if (Number.class.isAssignableFrom(propertyType) || double.class == propertyType || int.class == propertyType) {
                    yield new IBeforeChangeEventHandler[] { createMaxValueValidator(((Max) annotation).value()) };
                } else if (String.class == propertyType) {
                    yield new IBeforeChangeEventHandler[] { createMaxLengthValidator(((Max) annotation).value()) };
                }
                throw new EntityDefinitionException("Property [%s] of type [%s] does not support Max validation.".formatted(propertyName, propertyType.getName()));
            }
            case DOMAIN -> new IBeforeChangeEventHandler[] { domainValidationConfig.getValidator(entity.getType(), propertyName) };
            case BEFORE_CHANGE -> createBeforeChange(entity, propertyName, (BeforeChange) annotation);
            case UNIQUE -> new IBeforeChangeEventHandler[] { injector.getInstance(UniqueValidator.class) };
            default -> throw new EntityDefinitionException(ERR_UNSUPPORTED_VALIDATION_ANNOTATION);
        };
    }

    protected IBeforeChangeEventHandler<?>[] createFinalValidator(final AbstractEntity<?> entity, final String propertyName, final Final annotation) {
        if (annotation.persistedOnly() && !entity.isPersistent() && !isSyntheticBasedOnPersistentEntityType(entity.getType())) {
            throw new EntityDefinitionException(format("Non-persistent entity [%s] has property [%s], which is incorrectly annotated with @Final(persistentOnly = true).", entity.getType().getSimpleName(), propertyName));
        }
        if (annotation.persistedOnly()) {
            return annotation.nullIsValueForPersisted() ? persistedOnlyAndNullIsValueFinalValidator : persistedOnlyFinalValidator;
        } else {
            return annotation.nullIsValueForPersisted() ? notPersistedOnlyAndNullIsValueFinalValidator : notPersistedOnlyFinalValidator;
        }
    }

    /**
     * Creates validators declared as BCE handlers.
     *
     * @param entity
     * @param propertyName
     * @param annotation
     * @return
     */
    private IBeforeChangeEventHandler<?>[] createBeforeChange(final AbstractEntity<?> entity, final String propertyName, final BeforeChange annotation) {
        // 0. If the cache contains handlers for the entity and property then return them. Otherwise, step 1.
        final Map<String, IBeforeChangeEventHandler<?>[]> typeHandlers = beforeChangeEventHandlers.get(entity.getType());
        if (typeHandlers != null && typeHandlers.containsKey(propertyName)) {
            return typeHandlers.get(propertyName);
        }
        // 1. BeforeChange annotations has property <code>value</code>, which is an array of annotations Handler.
        //    Need to iterate over all these handler-annotations for instantiation of event handlers.
        final Handler[] handlerDeclarations = annotation.value();
        // 2. For each event handler do
        //    2.1 Instantiate a handler using injector for property <code>value</code>, which contains handler's class declaration
        //    2.2 For each value in arrays <code>non_ordinary</code>, <code>integer</code>, <code>str</code>, <code>dbl</code>, <code>date</code>, <code>date_time</code>, <code>money</code>
        //    initialise handler's parameters.
        final IBeforeChangeEventHandler<?>[] handlers = new IBeforeChangeEventHandler[handlerDeclarations.length];
        for (int index = 0; index < handlerDeclarations.length; index++) {
            final Handler hd = handlerDeclarations[index];
            final IBeforeChangeEventHandler<?> handler = injector.getInstance(hd.value());
            initNonOrdinaryHandlerParameters(entity, hd.non_ordinary(), handler);
            initClassHandlerParameters(entity, hd.clazz(), handler);
            initIntegerHandlerParameters(entity, hd.integer(), handler);
            initDoubleHandlerParameters(entity, hd.dbl(), handler);
            initStringHandlerParameters(entity, hd.str(), handler);
            initBooleanHandlerParameters(entity, hd.bool(), handler);
            initPropHandlerParameters(entity, hd.prop(), handler);
            initDateHandlerParameters(entity, hd.date(), handler);
            initDateTimeHandlerParameters(entity, hd.date_time(), handler);
            initMoneyHandlerParameters(entity, hd.money(), handler);
            initEnumHandlerParameters(entity, hd.enumeration(), handler);


            handlers[index] = handler;
        }
        // 3. Cache all instantiated handlers against the entity and property.
        if (typeHandlers == null) { // currently there are no handlers associate with any of the type properties
            // the use of LinkedHashMap is critical in order to maintain the order of BCE handlers
            final Map<String, IBeforeChangeEventHandler<?>[]> newTypeHandlers = new LinkedHashMap<>();
            beforeChangeEventHandlers.put(entity.getType(), newTypeHandlers);
        }
        beforeChangeEventHandlers.get(entity.getType()).put(propertyName, handlers);

        // 4. Return an array of instantiated handlers.
        return handlers;
    }

    /**
     * Initialises non-ordinary handler parameters as provided in {@link Handler#non_ordinary()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initNonOrdinaryHandlerParameters(final AbstractEntity<?> entity, final ClassParam[] params, final Object handler) {
        for (final ClassParam param : params) {
            final Class<?> type = param.value();
            if (IBeforeChangeEventHandler.class.isAssignableFrom(type)) {
                throw new PropertyBceOrAceDefinitionException(ERR_HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }
            if (IAfterChangeEventHandler.class.isAssignableFrom(type)) {
                throw new PropertyBceOrAceDefinitionException(ERR_HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }

            final Object value = injector.getInstance(type);
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, value);
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises handler parameters of type Class as provided in {@link Handler#clazz()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initClassHandlerParameters(final AbstractEntity<?> entity, final ClassParam[] params, final Object handler) {
        for (final ClassParam param : params) {
            final Class<?> type = param.value();
            if (IBeforeChangeEventHandler.class.isAssignableFrom(type)) {
                throw new PropertyBceOrAceDefinitionException(ERR_HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }
            if (IAfterChangeEventHandler.class.isAssignableFrom(type)) {
                throw new PropertyBceOrAceDefinitionException(ERR_HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }

            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, type);
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises integer handler parameters as provided in {@link Handler#integer()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initIntegerHandlerParameters(final AbstractEntity<?> entity, final IntParam[] params, final Object handler) {
        for (final IntParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises double handler parameters as provided in {@link Handler#dbl()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initDoubleHandlerParameters(final AbstractEntity<?> entity, final DblParam[] params, final Object handler) {
        for (final DblParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link String} handler parameters as provided in {@link Handler#str()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initStringHandlerParameters(final AbstractEntity<?> entity, final StrParam[] params, final Object handler) {
        for (final StrParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link boolean} handler parameters as provided in {@link Handler#bool()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initBooleanHandlerParameters(final AbstractEntity<?> entity, final BooleanParam[] params, final Object handler) {
        for (final BooleanParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@code property-driven} handler parameters as provided in {@link Handler#prop()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initPropHandlerParameters(final AbstractEntity<?> entity, final PropParam[] params, final Object handler) {
        for (final PropParam param : params) {
            final String propName = param.propName();
            if (!Finder.isPropertyPresent(entity.getClass(), propName)) {
                throw new PropertyBceOrAceDefinitionException(format(ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM, propName, entity.getType().getName()));
            }
            
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.propName());
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException(format("Could not initialise parameter %s@%s", param.name(), handler.getClass().getName()), ex);
            }
        }
    }

    /**
     * Initialises {@link Date} handler parameters as provided in {@link Handler#date()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initDateHandlerParameters(final AbstractEntity<?> entity, final DateParam[] params, final Object handler) {
        for (final DateParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, StringConverter.toDate(param.value(), dates));
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link DateTime} handler parameters as provided in {@link Handler#date_time()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initDateTimeHandlerParameters(final AbstractEntity<?> entity, final DateTimeParam[] params, final Object handler) {
        for (final DateTimeParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, StringConverter.toDateTime(param.value(), dates));
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link Money} handler parameters as provided in {@link Handler#money()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private void initMoneyHandlerParameters(final AbstractEntity<?> entity, final MoneyParam[] params, final Object handler) {
        for (final MoneyParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, StringConverter.toMoney(param.value()));
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises enumeration handler parameters as provided in {@link Handler#enumeration()}.
     *
     * @param entity
     * @param params
     * @param handler
     */
    private <T extends Enum<T>> void initEnumHandlerParameters(final AbstractEntity<?> entity, final EnumParam[] params, final Object handler) {
        for (final EnumParam param : params) {
            @SuppressWarnings("unchecked") // this type casting is the best we can do in order to make the compiler happy
            final Class<T> enumType = (Class<T>) param.clazz();

            final Enum<?> value;
            try {
                value = Enum.valueOf(enumType, param.value());
            } catch (final Exception e) {
                throw new PropertyBceOrAceDefinitionException(format("Value [%s] is not of type [%s].", param.value(), enumType.getName()));
            }

            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, value);
            } catch (final Exception ex) {
                throw new PropertyBceOrAceDefinitionException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    private IBeforeChangeEventHandler<?> createGePropertyValidator(final AbstractEntity<?> entity,
                                                                   final String upperBoundaryProperty,
                                                                   final Class<?> upperBoundaryPropertyType,
                                                                   final String[] lowerBoundaryProperties) {
        return geRangeValidators
                .computeIfAbsent(entity.getType(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(upperBoundaryProperty,
                                 key -> injector.getInstance(GePropertyValidator.Factory.class)
                                         .create(lowerBoundaryProperties,
                                                 injector.getInstance(RangeValidatorFunction.forPropertyType(upperBoundaryPropertyType))));
    }

    private IBeforeChangeEventHandler<?> createLePropertyValidator(final AbstractEntity<?> entity,
                                                                   final String lowerBoundaryProperty,
                                                                   final Class<?> lowerBoundaryPropertyType,
                                                                   final String[] upperBoundaryProperties) {
        return leRangeValidators
                .computeIfAbsent(entity.getType(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(lowerBoundaryProperty,
                                 key -> injector.getInstance(LePropertyValidator.Factory.class)
                                         .create(upperBoundaryProperties,
                                                 injector.getInstance(RangeValidatorFunction.forPropertyType(lowerBoundaryPropertyType))));


    }

    private IBeforeChangeEventHandler<?> createGreaterOrEqualValidator(final Integer key) {
        return greaterOrEqualsValidators.computeIfAbsent(key, GreaterOrEqualValidator::new);
    }

    private IBeforeChangeEventHandler<?> createMaxLengthValidator(final Integer key) {
        return maxLengthValidators.computeIfAbsent(key, MaxLengthValidator::new);
    }

    private IBeforeChangeEventHandler<?> createMaxValueValidator(final Integer key) {
        return maxValueValidators.computeIfAbsent(key, MaxValueValidator::new);
    }

    protected abstract IBeforeChangeEventHandler<?> createEntityExists(final EntityExists anotation);

    @Override
    public IAfterChangeEventHandler<?> create(final AbstractEntity<?> entity, final Field propertyField) throws Exception {
        // let's first check the old way of registering property definers
        final String propertyName = propertyField.getName();
        final IAfterChangeEventHandler<?> handler = domainMetaConfig.getDefiner(entity.getType(), propertyName);
        if (handler != null) {
            return handler;
        }
        // if not provided, then need to follow the new way of instantiating and caching ACE handlers
        final Class<?> type = entity.getType();
        Map<String, IAfterChangeEventHandler<?>> typeHandlers = afterChangeEventHandlers.get(type);
        if (typeHandlers == null) {
            typeHandlers = new ConcurrentHashMap<>();
            afterChangeEventHandlers.put(entity.getType(), typeHandlers);
        }
        IAfterChangeEventHandler<?> propHandler = typeHandlers.get(propertyName);
        if (propHandler == null) {

            final AfterChange ach = AnnotationReflector.getAnnotation(propertyField, AfterChange.class);
            if (ach == null) {
                return null;
            }
            // instantiate ACE handler
            propHandler = injector.getInstance(ach.value());
            // initialise ACE handler parameters
            initNonOrdinaryHandlerParameters(entity, ach.non_ordinary(), propHandler);
            initClassHandlerParameters(entity, ach.clazz(), propHandler);
            initIntegerHandlerParameters(entity, ach.integer(), propHandler);
            initDoubleHandlerParameters(entity, ach.dbl(), propHandler);
            initStringHandlerParameters(entity, ach.str(), propHandler);
            initPropHandlerParameters(entity, ach.prop(), propHandler);
            initDateHandlerParameters(entity, ach.date(), propHandler);
            initDateTimeHandlerParameters(entity, ach.date_time(), propHandler);
            initMoneyHandlerParameters(entity, ach.money(), propHandler);
            initEnumHandlerParameters(entity, ach.enumeration(), propHandler);

            // associate handler with property name
            typeHandlers.put(propertyName, propHandler);
        }

        return propHandler;
    }

}
