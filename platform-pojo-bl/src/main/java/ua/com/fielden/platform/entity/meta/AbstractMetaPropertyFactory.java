package ua.com.fielden.platform.entity.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateTimeParam;
import ua.com.fielden.platform.entity.annotation.mutator.DblParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.annotation.mutator.MoneyParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.FinalValidator;
import ua.com.fielden.platform.entity.validation.GreaterOrEqualValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.entity.validation.MaxValueValidator;
import ua.com.fielden.platform.entity.validation.RangePropertyValidator;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.StringConverter;

import com.google.inject.Injector;

/**
 * Base implementation for {@link IMetaPropertyFactory}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractMetaPropertyFactory implements IMetaPropertyFactory {

    public static final String UNSUPPORTED_VALIDATION_ANNOTATION = "Unsupported validation annotation has been encountered.";
    public static final String UNRECOGNISED_VALIDATION_ANNOTATION = "Unrecognised validation annotation has been encountered.";
    public static final String INJECTOR_IS_MISSING = "Meta-property factory is not fully initialised -- injector is missing";
    public static final String HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER = "BCE/ACE handlers should not have a another BCE/ACE handler as its parameter.";

    protected final FinalValidator finalValidator = new FinalValidator();
    protected final Map<Class<? extends AbstractEntity<?>>, EntityExistsValidator> entityExistsValidators = Collections.synchronizedMap(new HashMap<>());
    protected final Map<Integer, GreaterOrEqualValidator> greaterOrEqualsValidators = Collections.synchronizedMap(new HashMap<Integer, GreaterOrEqualValidator>());
    protected final Map<Integer, MaxLengthValidator> maxLengthValidators = Collections.synchronizedMap(new HashMap<Integer, MaxLengthValidator>());
    protected final Map<Integer, MaxValueValidator> maxValueValidators = Collections.synchronizedMap(new HashMap<Integer, MaxValueValidator>());
    protected final Map<Class<?>, Map<String, RangePropertyValidator>> geRangeValidators = Collections.synchronizedMap(new HashMap<Class<?>, Map<String, RangePropertyValidator>>());
    protected final Map<Class<?>, Map<String, RangePropertyValidator>> leRangeValidators = Collections.synchronizedMap(new HashMap<Class<?>, Map<String, RangePropertyValidator>>());
    // type, property, array of handlers
    protected final Map<Class<?>, Map<String, IBeforeChangeEventHandler<?>[]>> beforeChangeEventHandlers = Collections.synchronizedMap(new HashMap<>());
    protected final Map<Class<?>, Map<String, IAfterChangeEventHandler<?>>> afterChangeEventHandlers = Collections.synchronizedMap(new HashMap<>());

    private Injector injector;

    protected final DomainValidationConfig domainConfig;
    protected final DomainMetaPropertyConfig domainMetaConfig;

    public AbstractMetaPropertyFactory(final DomainValidationConfig domainConfig, final DomainMetaPropertyConfig domainMetaConfig) {
        this.domainConfig = domainConfig;
        this.domainMetaConfig = domainMetaConfig;
    }

    @Override
    public IBeforeChangeEventHandler<?>[] create(//
    final Annotation annotation,//
            final AbstractEntity<?> entity,//
            final String propertyName,//
            final Class<?> propertyType) throws Exception {
        if (injector == null) {
            throw new IllegalStateException(INJECTOR_IS_MISSING);
        }
        // identify the type of annotation
        ValidationAnnotation value = null;
        for (final ValidationAnnotation validationAnnotation : ValidationAnnotation.values()) {
            if (validationAnnotation.getType().equals(annotation.annotationType())) {
                value = validationAnnotation;
            }
        }
        // check whether it can be recognised as a valid annotation permitted for validation purpose
        if (value == null) {
            throw new IllegalArgumentException(UNRECOGNISED_VALIDATION_ANNOTATION);
        }
        // try to instantiate validator
        switch (value) {
        //case NOT_NULL:
        //    return new IBeforeChangeEventHandler[] { notNullValidator };
        case ENTITY_EXISTS:
            return new IBeforeChangeEventHandler[] { createEntityExists((EntityExists) annotation) };
        case FINAL:
            return new IBeforeChangeEventHandler[] { finalValidator };
        case GREATER_OR_EQUAL:
            return new IBeforeChangeEventHandler[] { createGreaterOrEqualValidator(((GreaterOrEqual) annotation).value()) };
        case LE_PROPETY:
            return new IBeforeChangeEventHandler[] { createLePropertyValidator(entity, propertyName, ((LeProperty) annotation).value()) };
        case GE_PROPETY:
            return new IBeforeChangeEventHandler[] { createGePropertyValidator(entity, ((GeProperty) annotation).value(), propertyName) };
        case MAX:
            if (Number.class.isAssignableFrom(propertyType) || double.class == propertyType || int.class == propertyType) {
                return new IBeforeChangeEventHandler[] { createMaxValueValidator(((Max) annotation).value()) };
            } else if (String.class == propertyType) {
                return new IBeforeChangeEventHandler[] { createMaxLengthValidator(((Max) annotation).value()) };
            }
            throw new RuntimeException("Property " + propertyName + " of type " + propertyType.getName() + " does not support Max validation.");
        case DOMAIN:
            return new IBeforeChangeEventHandler[] { domainConfig.getValidator(entity.getType(), propertyName) };
        case BEFORE_CHANGE:
            return createBeforeChange(entity, propertyName, (BeforeChange) annotation);
        default:
            throw new IllegalArgumentException(UNSUPPORTED_VALIDATION_ANNOTATION);
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
        //	  initialise handler's parameters.
        final IBeforeChangeEventHandler<?>[] handlers = new IBeforeChangeEventHandler[handlerDeclarations.length];
        for (int index = 0; index < handlerDeclarations.length; index++) {
            final Handler hd = handlerDeclarations[index];
            final IBeforeChangeEventHandler<?> handler = injector.getInstance(hd.value());
            initNonOrdinaryHandlerParameters(entity, hd.non_ordinary(), handler);
            initClassHandlerParameters(entity, hd.clazz(), handler);
            initIntegerHandlerParameters(entity, hd.integer(), handler);
            initDoubleHandlerParameters(entity, hd.dbl(), handler);
            initStringHandlerParameters(entity, hd.str(), handler);
            initDateHandlerParameters(entity, hd.date(), handler);
            initDateTimeHandlerParameters(entity, hd.date_time(), handler);
            initMoneyHandlerParameters(entity, hd.money(), handler);

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
     * @param hd
     * @param handler
     */
    private void initNonOrdinaryHandlerParameters(final AbstractEntity<?> entity, final ClassParam[] params, final Object handler) {
        for (final ClassParam param : params) {
            final Class<?> type = param.value();
            if (IBeforeChangeEventHandler.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }
            if (IAfterChangeEventHandler.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }

            final Object value = injector.getInstance(type);
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, value);
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises handler parameters of type Class as provided in {@link Handler#clazz()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initClassHandlerParameters(final AbstractEntity<?> entity, final ClassParam[] params, final Object handler) {
        for (final ClassParam param : params) {
            final Class<?> type = param.value();
            if (IBeforeChangeEventHandler.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }
            if (IAfterChangeEventHandler.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(HANDLER_WITH_ANOTHER_HANDLER_AS_PARAMETER);
            }

            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, type);
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises integer handler parameters as provided in {@link Handler#integer()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initIntegerHandlerParameters(final AbstractEntity<?> entity, final IntParam[] params, final Object handler) {
        for (final IntParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises double handler parameters as provided in {@link Handler#dbl()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initDoubleHandlerParameters(final AbstractEntity<?> entity, final DblParam[] params, final Object handler) {
        for (final DblParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link String} handler parameters as provided in {@link Handler#str()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initStringHandlerParameters(final AbstractEntity<?> entity, final StrParam[] params, final Object handler) {
        for (final StrParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, param.value());
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link Date} handler parameters as provided in {@link Handler#date()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initDateHandlerParameters(final AbstractEntity<?> entity, final DateParam[] params, final Object handler) {
        for (final DateParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, StringConverter.toDate(param.value()));
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link DateTime} handler parameters as provided in {@link Handler#date_time()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initDateTimeHandlerParameters(final AbstractEntity<?> entity, final DateTimeParam[] params, final Object handler) {
        for (final DateTimeParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, StringConverter.toDateTime(param.value()));
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    /**
     * Initialises {@link Money} handler parameters as provided in {@link Handler#money()}.
     *
     * @param entity
     * @param hd
     * @param handler
     */
    private void initMoneyHandlerParameters(final AbstractEntity<?> entity, final MoneyParam[] params, final Object handler) {
        for (final MoneyParam param : params) {
            final Field paramField = Finder.getFieldByName(handler.getClass(), param.name());
            paramField.setAccessible(true);
            try {
                paramField.set(handler, StringConverter.toMoney(param.value()));
            } catch (final Exception ex) {
                throw new IllegalStateException("Could not initialise parameter " + param.name() + "@" + handler.getClass().getName(), ex);
            }
        }
    }

    private IBeforeChangeEventHandler<?> createGePropertyValidator(final AbstractEntity<?> entity, final String[] lowerBoundaryProperties, final String upperBoundaryProperty) {
        if (geRangeValidators.get(entity.getType()) == null) {
            geRangeValidators.put(entity.getType(), Collections.synchronizedMap(new HashMap<String, RangePropertyValidator>()));
        }
        final Map<String, RangePropertyValidator> propertyValidators = geRangeValidators.get(entity.getType());
        if (propertyValidators.get(upperBoundaryProperty) == null) {
            propertyValidators.put(upperBoundaryProperty, new RangePropertyValidator(lowerBoundaryProperties, true));
        }
        return propertyValidators.get(upperBoundaryProperty);
    }

    private IBeforeChangeEventHandler<?> createLePropertyValidator(final AbstractEntity<?> entity, final String lowerBoundaryProperty, final String[] upperBoundaryProperties) {
        if (leRangeValidators.get(entity.getType()) == null) {
            leRangeValidators.put(entity.getType(), Collections.synchronizedMap(new HashMap<String, RangePropertyValidator>()));
        }
        final Map<String, RangePropertyValidator> propertyValidators = leRangeValidators.get(entity.getType());
        if (propertyValidators.get(lowerBoundaryProperty) == null) {
            propertyValidators.put(lowerBoundaryProperty, new RangePropertyValidator(upperBoundaryProperties, false));
        }
        return propertyValidators.get(lowerBoundaryProperty);
    }

    private IBeforeChangeEventHandler<?> createGreaterOrEqualValidator(final Integer key) {
        if (!greaterOrEqualsValidators.containsKey(key)) {
            greaterOrEqualsValidators.put(key, new GreaterOrEqualValidator(key));
        }
        return greaterOrEqualsValidators.get(key);
    }

    private IBeforeChangeEventHandler<?> createMaxLengthValidator(final Integer key) {
        if (!maxLengthValidators.containsKey(key)) {
            maxLengthValidators.put(key, new MaxLengthValidator(key));
        }
        return maxLengthValidators.get(key);
    }

    private IBeforeChangeEventHandler<?> createMaxValueValidator(final Integer key) {
        if (!maxValueValidators.containsKey(key)) {
            maxValueValidators.put(key, new MaxValueValidator(key));
        }
        return maxValueValidators.get(key);
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
        // if not provided then need to follow the new way of instantiating and caching ACE handlers
        final Class<?> type = entity.getType();
        Map<String, IAfterChangeEventHandler<?>> typeHandlers = afterChangeEventHandlers.get(type);
        if (typeHandlers == null) {
            typeHandlers = new HashMap<>();
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
            initDateHandlerParameters(entity, ach.date(), propHandler);
            initDateTimeHandlerParameters(entity, ach.date_time(), propHandler);
            initMoneyHandlerParameters(entity, ach.money(), propHandler);

            // associate handler with property name
            typeHandlers.put(propertyName, propHandler);
        }

        return propHandler;
    }

    @Override
    public void setInjector(final Injector injector) {
        this.injector = injector;
    }
}
