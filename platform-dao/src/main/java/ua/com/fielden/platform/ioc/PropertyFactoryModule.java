package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.property.DaoMetaPropertyFactory;

import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule extends TransactionalModule {

    protected final DaoFactory daoFactory;
    protected final EntityFactory entityFactory;
    protected final DefaultCompanionObjectFinderImpl defaultControllerProvider;

    public PropertyFactoryModule(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes)
            throws Exception {
        super(props, defaultHibernateTypes, applicationEntityTypes);
        entityFactory = new EntityFactory() {};
        daoFactory = new DaoFactory() {};
        defaultControllerProvider = new DefaultCompanionObjectFinderImpl();

        initHibernateConfig(entityFactory);
    }

    public PropertyFactoryModule(final SessionFactory sessionFactory, final DomainMetadata domainMetadata) {
        super(sessionFactory, domainMetadata);
        daoFactory = new DaoFactory() {
        };
        entityFactory = new EntityFactory() {
        };
        defaultControllerProvider = new DefaultCompanionObjectFinderImpl();
    }

    @Override
    protected void configure() {
        super.configure();
        bind(EntityFactory.class).toInstance(entityFactory);
        // bind DaoFactory, which is needed purely for MetaPropertyFactory
        bind(DaoFactory.class).toInstance(daoFactory);
        // bind provider for default entity controller
        bind(ICompanionObjectFinder.class).toInstance(defaultControllerProvider);
        // bind property factory
        bind(IMetaPropertyFactory.class).to(DaoMetaPropertyFactory.class).in(Scopes.SINGLETON);
        // bind entity aggregates DAO
        bind(IEntityAggregatesDao.class).to(EntityAggregatesDao.class);

    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        daoFactory.setInjector(injector);
        entityFactory.setInjector(injector);
        defaultControllerProvider.setInjector(injector);
        final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
        mfp.setInjector(injector);
    }
}
