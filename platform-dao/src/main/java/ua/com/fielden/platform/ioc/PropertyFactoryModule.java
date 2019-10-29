package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;

import com.google.inject.Injector;
import com.google.inject.Scopes;

import ua.com.fielden.platform.dao.CommonEntityAggregatesDao;
import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.property.DefaultMetaPropertyFactory;
import ua.com.fielden.platform.entity.query.IEntityAggregates;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule extends TransactionalModule {

    protected final EntityFactory entityFactory;

    public PropertyFactoryModule(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) {
        super(props, defaultHibernateTypes, applicationEntityTypes);
        entityFactory = new EntityFactory() {};

        initHibernateConfig(entityFactory);
    }

    public PropertyFactoryModule(final SessionFactory sessionFactory, final DomainMetadata domainMetadata, final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        super(sessionFactory, domainMetadata, idOnlyProxiedEntityTypeCache);
        entityFactory = new EntityFactory() {};
    }

    @Override
    protected void configure() {
        super.configure();
        bind(EntityFactory.class).toInstance(entityFactory);
        // bind provider for default entity controller
        bind(ICompanionObjectFinder.class).to(DefaultCompanionObjectFinderImpl.class);
        // bind property factory
        bind(IMetaPropertyFactory.class).to(DefaultMetaPropertyFactory.class).in(Scopes.SINGLETON);
        // bind entity aggregates DAO
        bind(IEntityAggregatesOperations.class).to(EntityAggregatesDao.class);
        bind(IEntityAggregates.class).to(CommonEntityAggregatesDao.class);

    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        entityFactory.setInjector(injector);
        final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
        mfp.setInjector(injector);
    }
}
