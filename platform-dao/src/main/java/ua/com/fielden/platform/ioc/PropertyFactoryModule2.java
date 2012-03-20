package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.factory.DaoFactory2;
import ua.com.fielden.platform.dao2.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.DefaultConrollerProviderImpl2;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IDefaultControllerProvider2;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.property.DaoMetaPropertyFactory2;

import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule2 extends TransactionalModule2 {

    protected final DaoFactory2 daoFactory;
    protected final EntityFactory entityFactory;
    protected final DefaultConrollerProviderImpl2 defaultControllerProvider;

    public PropertyFactoryModule2(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity>> applicationEntityTypes) throws Exception {
	super(props, defaultHibernateTypes, applicationEntityTypes);
	entityFactory = new EntityFactory() {};
	daoFactory = new DaoFactory2() {};
	defaultControllerProvider = new DefaultConrollerProviderImpl2();
	interceptor.setFactory(entityFactory);
    }

    public PropertyFactoryModule2(final SessionFactory sessionFactory, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingsGenerator);
	daoFactory = new DaoFactory2() {};
	entityFactory = new EntityFactory() {};
	defaultControllerProvider = new DefaultConrollerProviderImpl2();
    }

    @Override
    protected void configure() {
	super.configure();
	bind(EntityFactory.class).toInstance(entityFactory);
	// bind DaoFactory, which is needed purely for MetaPropertyFactory
	bind(DaoFactory2.class).toInstance(daoFactory);
	// bind provider for default entity controller
	bind(IDefaultControllerProvider2.class).toInstance(defaultControllerProvider);
	// bind property factory
	bind(IMetaPropertyFactory.class).to(DaoMetaPropertyFactory2.class).in(Scopes.SINGLETON);

    }

    @Override
    public void setInjector(final Injector injector) {
	daoFactory.setInjector(injector);
	entityFactory.setInjector(injector);
	defaultControllerProvider.setInjector(injector);
	final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
	mfp.setInjector(injector);
    }
}
