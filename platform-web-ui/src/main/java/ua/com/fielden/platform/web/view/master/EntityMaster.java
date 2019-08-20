package ua.com.fielden.platform.web.view.master;

import static ua.com.fielden.platform.utils.EntityUtils.fetchNone;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerForCompoundMenuItem;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.AbstractEntityAutocompletionWidget;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> implements IRenderable {
    public static final String ENTITY_TYPE = "@entity_type";
    private final Class<T> entityType;
    private final Class<? extends IEntityProducer<T>> entityProducerType;
    private final IMaster<T> masterConfig;
    private final ICompanionObjectFinder coFinder;
    private final Injector injector;

    /**
     * Creates master for the specified <code>entityType</code>, <code>smConfig</code> and <code>entityProducerType</code>.
     *
     * @param entityType
     * @param entityProducerType
     * @param masterConfig
     *
     */
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
    
    /**
     * A convenience factory method for actions (implemented as functional entities) without the UI part (the master is still required to capture the execution context).
     * 
     *
     * @param entityType
     * @param entityProducerType
     * @param injector
     * @param customCode -- custom JS code to be executed after master component creation.
     * @param customCodeOnAttach -- custom JS code to be executed every time master component is attached to client application's DOM
     * 
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final Injector injector,
            final JsCode customCode, 
            final JsCode customCodeOnAttach
            ) {
        return new EntityMaster<>(entityType, entityProducerType, new NoUiMaster<>(entityType, customCode, customCodeOnAttach), injector);
    }
    
    /**
     * A convenience factory method for actions (implemented as functional entities) without the UI part (the master is still required to capture the execution context).
     *
     * @param entityType
     * @param entityProducerType
     * @param injector
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final Injector injector) {
        return new EntityMaster<>(entityType, entityProducerType, null, injector);
    }

    /**
     * A convenience factory method for actions (implemented as functional entities) without the UI part (the master is still required to capture the execution context).
     * <p>
     * This version specifies no producer, which means that default one will be used.
     *
     * @param entityType
     * @param injector
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Injector injector) {
        return new EntityMaster<>(entityType, null, null, injector);
    }

    private IMaster<T> createDefaultConfig(final Class<T> entityType) {
        return new NoUiMaster<>(entityType);
    }

    /**
     * Creates master for the specified <code>entityType</code> and <code>smConfig</code> (no producer).
     *
     * @param entityType
     * @param config
     *
     */
    public EntityMaster(final Class<T> entityType, final IMaster<T> config, final Injector injector) {
        this(entityType, null, config, injector);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Creates an entity producer instance.
     *
     * @param injector
     * @return
     */
    public IEntityProducer<T> createEntityProducer() {
        return entityProducerType == null ? createDefaultEntityProducer(injector.getInstance(EntityFactory.class), this.entityType, this.coFinder)
                : injector.getInstance(this.entityProducerType);
    }

    /**
     * Creates default entity producer instance.
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> IEntityProducer<T> createDefaultEntityProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder coFinder) {
        if (AbstractFunctionalEntityForCompoundMenuItem.class.isAssignableFrom(entityType)) {
            return new DefaultEntityProducerForCompoundMenuItem(factory, entityType, coFinder);
        }
        return new DefaultEntityProducerWithContext<>(factory, entityType, coFinder);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public IValueMatcherWithContext<T, ?> createValueMatcher(final String propertyName) {
        final Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherType = masterConfig.matcherTypeFor(propertyName);
        if (matcherType.isPresent()) {
            return injector.getInstance(matcherType.get());
        }

        return createDefaultValueMatcher(propertyName, entityType, coFinder);
    }
    
    /**
     * Creates fetch model for entity-typed autocompleted values. Fetches only properties specified in Master DSL configuration.
     *
     * @param originalPropertyName
     * @param propType
     * @return
     */
    public <V extends AbstractEntity<?>> fetch<V> createFetchModelForAutocompleter(final String originalPropertyName, final Class<V> propType) {
        final Optional<WidgetSelector> widgetSelectorOpt = masterConfig.widgetFor(originalPropertyName);
        if (widgetSelectorOpt.isPresent()) {
            final WidgetSelector widgetSelector = widgetSelectorOpt.get();
            if (widgetSelector.widget() instanceof AbstractEntityAutocompletionWidget) {
                final AbstractEntityAutocompletionWidget widget = (AbstractEntityAutocompletionWidget) widgetSelector.widget();
                final IFetchProvider<V> fetchProvider = fetchNone(propType).with(widget.additionalProps.keySet());
                fetchProvider.addKeysTo("");
                for (final String additionalProperty: widget.additionalProps.keySet()) {
                    fetchProvider.addKeysTo(additionalProperty);
                }
                System.out.println("fetchProvider = " + fetchProvider);
                System.out.println("fetchModel = " + fetchProvider.fetchModel());
                return fetchProvider.fetchModel();
            }
        }
        return null;
    }

    /**
     * Creates default value matcher with context for the specified entity property.
     *
     * @param propertyName
     * @param entityType
     * @param coFinder
     * @return
     */
    public static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IValueMatcherWithContext<T, V> createDefaultValueMatcher(
            final String propertyName, 
            final Class<T> entityType, 
            final ICompanionObjectFinder coFinder) {
        
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<V> propertyType = (Class<V>) (isEntityItself ? entityType : PropertyTypeDeterminator.determinePropertyType(entityType, propertyName));
        final IEntityDao<V> co = coFinder.find(propertyType);

        // filtering out of inactive should only happen for activatable properties without SkipEntityExistsValidation present
        final boolean activeOnly = 
                ActivatableAbstractEntity.class.isAssignableFrom(propertyType) &&
                !Finder.findFieldByName(entityType, propertyName).isAnnotationPresent(SkipEntityExistsValidation.class);
        
        return new FallbackValueMatcherWithContext<>(co, activeOnly);
    }

    @Override
    public DomElement render() {
        return masterConfig.render().render();
    }

    public static String flattenedNameOf(final Class<?> type) {
        return type.getSimpleName().toLowerCase();
    }

    /**
     * An entity master that has no UI. Its main purpose is to be used for functional entities that have no visual representation.
     *
     * @author TG Team
     *
     * @param <T>
     */
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
        
        @Override
        public Optional<WidgetSelector> widgetFor(final String propertyName) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    /**
     * Returns action configuration for concrete action kind and its number in that kind's space.
     * 
     * @param actionKind
     * @param actionNumber
     * @return
     */
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        return masterConfig.actionConfig(actionKind, actionNumber);
    }
    
}
