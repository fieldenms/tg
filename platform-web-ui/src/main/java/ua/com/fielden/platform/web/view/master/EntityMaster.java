package ua.com.fielden.platform.web.view.master;

import com.google.inject.Injector;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackPropertyDescriptorMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentre;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableProperty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.centre.EntityCentre.createFetchModelForAutocompleterFrom;

/// Represents entity master.
///
public class EntityMaster<T extends AbstractEntity<?>> implements IRenderable {
    public static final String ENTITY_TYPE = "@entity_type";
    private final Class<T> entityType;
    private final Class<? extends IEntityProducer<T>> entityProducerType;
    private final IMaster<T> masterConfig;
    private final ICompanionObjectFinder coFinder;
    private final Injector injector;

    /// Creates master for the specified `entityType`, `smConfig` and `entityProducerType`.
    ///
    public EntityMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final IMaster<T> masterConfig,
            final Injector injector) {
        this.entityType = entityType;
        this.entityProducerType = entityProducerType;
        this.masterConfig = masterConfig == null ? createDefaultConfig(entityType) : masterConfig;
        this.coFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.injector = injector;
    }

    /// A convenience factory method for actions (implemented as functional entities) without the UI part (the master is still required to capture the execution context).
    ///
    /// @param customCode custom JS code to be executed after master component creation.
    /// @param customCodeOnAttach custom JS code to be executed every time master component is attached to client application's DOM
    ///
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final Injector injector,
            final JsCode customCode,
            final JsCode customCodeOnAttach
            ) {
        return new EntityMaster<>(entityType, entityProducerType, new NoUiMaster<>(entityType, customCode, customCodeOnAttach), injector);
    }

    /// A convenience factory method for actions (implemented as functional entities) without the UI part (the master is still required to capture the execution context).
    ///
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final Injector injector) {
        return new EntityMaster<>(entityType, entityProducerType, null, injector);
    }

    /// A convenience factory method for actions (implemented as functional entities) without the UI part (the master is still required to capture the execution context).
    ///
    /// This version specifies no producer, which means that default one will be used.
    ///
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Injector injector) {
        return new EntityMaster<>(entityType, null, null, injector);
    }

    private IMaster<T> createDefaultConfig(final Class<T> entityType) {
        return new NoUiMaster<>(entityType);
    }

    /// Creates master for the specified `entityType` and `smConfig` (no producer).
    ///
    public EntityMaster(final Class<T> entityType, final IMaster<T> config, final Injector injector) {
        this(entityType, null, config, injector);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    /// Creates an entity producer instance.
    ///
    public IEntityProducer<T> createEntityProducer() {
        return entityProducerType == null ? createDefaultEntityProducer(injector.getInstance(EntityFactory.class), this.entityType, this.coFinder)
                : injector.getInstance(this.entityProducerType);
    }

    /// Creates default entity producer instance.
    ///
    public static <T extends AbstractEntity<?>> IEntityProducer<T> createDefaultEntityProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder coFinder) {
        if (AbstractFunctionalEntityForCompoundMenuItem.class.isAssignableFrom(entityType)) {
            return new DefaultEntityProducerForCompoundMenuItem(factory, entityType, coFinder);
        }
        return new DefaultEntityProducerWithContext<>(factory, entityType, coFinder);
    }

    /// Creates value matcher instance.
    ///
    /// First, takes a manually registered value matcher instance.
    ///
    /// If empty, uses [IMaster#getAutocompleterAssociatedType(Class, String)] and creates default matcher for it.
    /// Please note, that [IMaster#getAutocompleterAssociatedType(Class, String)] may have been overridden.
    /// For example, it is overridden for String-typed entity editors (#2510).
    ///
    /// If still empty (unlikely), creates default matcher for it.
    ///
    public IValueMatcherWithContext<T, ?> createValueMatcher(final String propertyName) {
        return masterConfig.matcherTypeFor(propertyName)
                .<IValueMatcherWithContext<T, ?>> map(injector::getInstance)
                .or(() -> masterConfig.getAutocompleterAssociatedType(entityType, propertyName)
                                      .map(it -> createDefaultValueMatcherForPropType(it, propertyName, entityType, coFinder)))
                .orElseGet(() -> createDefaultValueMatcher(propertyName, entityType, coFinder));
    }

    /// Creates default Entity Master value matcher for concrete property type.
    /// The type may come from standard entity-typed properties with no custom matchers specified.
    /// Or it may come from String-typed properties with entity editor configurations (#2510).
    ///
    private static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IValueMatcherWithContext<T, V> createDefaultValueMatcherForPropType(final Class<V> propertyType, final String propertyName, final Class<T> entityType, final ICompanionObjectFinder coFinder) {
        // Create standard fallback `PropertyDescriptor` matcher, if the type is applicable.
        if (isPropertyDescriptor(propertyType)) {
            return (IValueMatcherWithContext<T, V>) new FallbackPropertyDescriptorMatcherWithContext<>((Class<AbstractEntity<?>>) getPropertyAnnotation(IsProperty.class, entityType, propertyName).value());
        }
        // Otherwise, create standard fallback matcher for all other types.
        else {
            // Filtering out of inactive values by default should only happen for activatable properties.
            // See `isActivatableProperty` for more.
            // Here, activatable unions are included, but @SkipEntityExistsValidation(skipActiveOnly) props excluded.
            return new FallbackValueMatcherWithContext<>(coFinder.find(propertyType), isActivatableProperty(entityType, propertyName));
        }
    }

    /// Creates fetch model for entity-typed autocompleted values. Fetches only properties specified in Master DSL configuration.
    ///
    public <V extends AbstractEntity<?>> fetch<V> createFetchModelForAutocompleter(final String propertyName, final Class<V> propType) {
        return createFetchModelForAutocompleterFrom(
                masterConfig.<V>getAutocompleterAssociatedType(entityType, propertyName).orElse(propType),
                masterConfig.additionalAutocompleterPropertiesFor(propertyName));
    }

    /// Creates default value matcher with context for the specified entity property.
    ///
    public static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IValueMatcherWithContext<T, V> createDefaultValueMatcher(
            final String propertyName,
            final Class<T> entityType,
            final ICompanionObjectFinder coFinder) {

        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<V> propertyType = (Class<V>) (isEntityItself ? entityType : determinePropertyType(entityType, propertyName));
        return createDefaultValueMatcherForPropType(propertyType, propertyName, entityType, coFinder);
    }

    @Override
    public DomElement render() {
        return masterConfig.render().render();
    }

    public static String flattenedNameOf(final Class<?> type) {
        return type.getSimpleName().toLowerCase();
    }

    /// An entity master that has no UI. Its main purpose is to be used for functional entities that have no visual representation.
    ///
    private static class NoUiMaster<T extends AbstractEntity<?>> implements IMaster<T> {

        private final IRenderable renderable;

        public NoUiMaster(final Class<T> entityType) {
            this(entityType, new JsCode(""), new JsCode(""));
        }

        public NoUiMaster(final Class<T> entityType, final JsCode customCode, final JsCode customCodeOnAttach) {
            final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                    .replace(IMPORTS, "")
                    .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                    .replace("<!--@tg-entity-master-content-->", "")
                    .replace("//@ready-callback", "")
                    .replace("//@master-is-ready-custom-code", customCode.toString())
                    .replace("//@master-has-been-attached-custom-code", customCodeOnAttach.toString())
                    .replace("@prefDim", "null")
                    .replace("@noUiValue", "true")
                    .replace("@saveOnActivationValue", "true");

            renderable = () -> new InnerTextElement(entityMasterStr);
        }

        @Override
        public IRenderable render() {
            return renderable;
        }

        @Override
        public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
            return Optional.empty();
        }

        @Override
        public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
            throw new UnsupportedOperationException("Getting of action configuration is not supported.");
        }
    }

    /// Returns action configuration for concrete action kind and its number in that kind's space.
    ///
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        return masterConfig.actionConfig(actionKind, actionNumber);
    }

    /// Returns embedded [EntityCentre] for this entity master, if any.
    ///
    public Optional<EntityCentre<?>> getEmbeddedCentre() {
        if (masterConfig instanceof MasterWithCentre) {
            return of(((MasterWithCentre<?>) masterConfig).embeddedCentre);
        }
        return empty();
    }

    /// Returns the map between property names and action selector for properties those have associated action.
    ///
    public Map<String, ? extends IEntityMultiActionSelector> getPropertyActionSelectors() {
        return masterConfig.propertyActionSelectors().entrySet().stream()
                .map(entry -> t2(entry.getKey(), injector.getInstance(entry.getValue())))
                .collect(toMap(tt -> tt._1, tt -> tt._2));
    }

}
