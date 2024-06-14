package ua.com.fielden.platform.ioc;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import ua.com.fielden.platform.dao.CommonEntityAggregatesDao;
import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.property.DefaultMetaPropertyFactory;
import ua.com.fielden.platform.entity.query.IEntityAggregates;

import java.util.Properties;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule extends TransactionalModule {

    protected final EntityFactory entityFactory;

    public PropertyFactoryModule(final Properties props) {
        super(props);
        entityFactory = new EntityFactory() {};
    }

    @Override
    protected void configure() {
        super.configure();
        bind(EntityFactory.class).toInstance(entityFactory);
        // bind provider for the default CO finder in singleton scope
        bind(ICompanionObjectFinder.class).to(DefaultCompanionObjectFinderImpl.class).in(Singleton.class);
        // bind property factory
        bind(IMetaPropertyFactory.class).to(DefaultMetaPropertyFactory.class).in(Singleton.class);
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
