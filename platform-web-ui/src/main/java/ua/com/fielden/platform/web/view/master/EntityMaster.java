package ua.com.fielden.platform.web.view.master;

import com.google.inject.Injector;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterConfig;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> implements IMaster<T> {
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

    private IMaster<T> createDefaultConfig(final Class<T> entityType) {
        return new NoUiMasterConfig<T>(entityType);
    }

    /**
     * Creates master for the specified <code>entityType</code> and <code>smConfig</code> (no producer).
     *
     * @param entityType
     * @param smConfig
     *
     */
    public EntityMaster(final Class<T> entityType, final ISimpleMasterConfig<T> smConfig, final Injector injector) {
        this(entityType, null, smConfig, injector);
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
        return entityProducerType == null ? createDefaultEntityProducer(injector.getInstance(EntityFactory.class), this.entityType)
                : injector.getInstance(this.entityProducerType);
    }

    /**
     * Creates default entity producer instance.
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> IEntityProducer<T> createDefaultEntityProducer(final EntityFactory factory, final Class<T> entityType) {
        return new DefaultEntityProducerWithContext<T, T>(factory, entityType);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public IValueMatcherWithContext<T, ?> createValueMatcher(final String propertyName) {
        if (masterConfig instanceof ISimpleMasterConfig) {
            final Class<? extends IValueMatcherWithContext<T, ?>> matcherType = ((ISimpleMasterConfig<T>) masterConfig).matcherTypeFor(propertyName);
            if (matcherType != null) {
                return injector.getInstance(matcherType);
            }
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
    public IRenderable render() {
        return masterConfig.render();
    }
}
