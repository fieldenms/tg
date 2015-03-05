package ua.com.fielden.platform.web.view.master;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.dao.DefaultEntityProducer;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterConfig;

import com.google.inject.Injector;

/**
 * Represents entity master.
 *
 * @author TG Team
 *
 */
public class EntityMaster<T extends AbstractEntity<?>> implements IMaster<T> {
    private final Class<T> entityType;
    private final Class<? extends IEntityProducer<T>> entityProducerType;
    private final ISimpleMasterConfig<T> smConfig;
    private final ICompanionObjectFinder coFinder;
    private final Injector injector;

    /**
     * Creates master for the specified <code>entityType</code> and <code>entityProducerType</code>.
     *
     * @param entityType
     * @param entityProducerType
     * @param masterComponent
     *
     */
    public EntityMaster(
            final Class<T> entityType,
            final Class<? extends IEntityProducer<T>> entityProducerType,
            final ISimpleMasterConfig<T> smConfig,
            final ICompanionObjectFinder coFinder,
            final Injector injector) {
        this.entityType = entityType;
        this.entityProducerType = entityProducerType;
        this.smConfig = smConfig;
        this.coFinder = coFinder;
        this.injector = injector;
    }

    /**
     * Creates master for the specified <code>entityType</code> and default entity producer.
     *
     * @param entityType
     * @param masterComponent
     *
     */
    public EntityMaster(final Class<T> entityType, final ISimpleMasterConfig<T> smConfig, final Injector injector) {
        this(entityType, null, smConfig, injector.getInstance(ICompanionObjectFinder.class), injector);
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
        return new DefaultEntityProducer<T>(factory, entityType);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public <V extends AbstractEntity<?>> IValueMatcherWithContext<T, V> createValueMatcher(final String propertyName) {
        final Class<IValueMatcherWithContext<T, V>> matcherType = smConfig.matcherTypeFor(propertyName);
        final IValueMatcherWithContext<T, V> matcher;
        if (matcherType != null) {
            matcher = injector.getInstance(matcherType);
        } else {
            matcher = createDefaultValueMatcher(propertyName, entityType, coFinder);
        }
        return matcher;
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
        final Class<V> propertyType = (Class<V>) PropertyTypeDeterminator.determinePropertyType(entityType, propertyName);
        final IEntityDao<V> co = coFinder.find(propertyType);
        return new FallbackValueMatcherWithContext<T, V>(co);
    }

    @Override
    public IRenderable build() {
        return smConfig.render();
    }

}
