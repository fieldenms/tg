package ua.com.fielden.platform.web.view.master;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.dao.DefaultEntityProducerForCompoundMenuItem;
import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> implements IRenderable {
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
     * @param entityType
     * @param entityProducerType
     * @param injector
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<T> noUiFunctionalMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final Injector injector) {
        return new EntityMaster<T>(entityType, entityProducerType, null, injector);
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
        return new EntityMaster<T>(entityType, null, null, injector);
    }
    
    private IMaster<T> createDefaultConfig(final Class<T> entityType) {
        return new NoUiMaster<T>(entityType);
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
        return new DefaultEntityProducerWithContext<T>(factory, entityType, coFinder);
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
     * Creates default value matcher with context for the specified entity property.
     *
     * @param propertyName
     * @param entityType
     * @param coFinder
     * @return
     */
    public static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IValueMatcherWithContext<T, V> createDefaultValueMatcher(final String propertyName, final Class<T> entityType, final ICompanionObjectFinder coFinder) {
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<V> propertyType = (Class<V>) (isEntityItself ? entityType : PropertyTypeDeterminator.determinePropertyType(entityType, propertyName));
        final IEntityDao<V> co = coFinder.find(propertyType);
        return new FallbackValueMatcherWithContext<T, V>(co);
    }

    @Override
    public DomElement render() {
        return masterConfig.render().render();
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
            final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                    .replace("<!--@imports-->", "")
                    .replace("@entity_type", entityType.getSimpleName())
                    .replace("<!--@tg-entity-master-content-->", "")
                    .replace("//@ready-callback", "")
                    .replace("@noUiValue", "true")
                    .replace("@saveOnActivationValue", "true");

            renderable = new IRenderable() {
                @Override
                public DomElement render() {
                    return new InnerTextElement(entityMasterStr);
                }
            };

        }

        @Override
        public IRenderable render() {
            return renderable;
        }

        @Override
        public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
            return Optional.empty();
        }

    }

}
