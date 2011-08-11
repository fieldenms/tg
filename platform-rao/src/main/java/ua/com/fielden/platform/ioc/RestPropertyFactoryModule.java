package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.entity.factory.DefaultConrollerProviderImpl;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IDefaultConrollerProvider;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.property.RaoMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.rao.RestClientUtil;

import com.google.inject.Scopes;

/**
 * Module for REST clients, which provides all essential binding such as lazy loading proxy intercepter and meta-property factory.
 *
 * @author TG Team
 *
 */
public class RestPropertyFactoryModule extends EntityModule {

    protected final EntityFactory entityFactory;
    protected final RestClientUtil restUtil;

    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();

    public RestPropertyFactoryModule(final RestClientUtil restUtil) {
	this.restUtil = restUtil;
	entityFactory = new EntityFactory() {
	};
    }

    @Override
    protected void configure() {
	super.configure();
	// TODO the Proxy interceptor should in future be bound to an implementation of REST lazy loading
	/*bindInterceptor(subclassesOf(AbstractEntity.class), // match only entity classes
	annotatedWith(Proxy.class), // having annotated methods
	new PropertyProxyInterceptor(sessionFactory) // the intercepter
	);*/
	bind(RestClientUtil.class).toInstance(restUtil);
	// bind DomainValidationConfig
	bind(DomainValidationConfig.class).toInstance(domainValidationConfig);
	// bind DomainMetaPropertyConfig
	bind(DomainMetaPropertyConfig.class).toInstance(domainMetaPropertyConfig);
	// bind provider for default entity controller
	bind(IDefaultConrollerProvider.class).to(DefaultConrollerProviderImpl.class).in(Scopes.SINGLETON);
	// bind property factory
	bind(IMetaPropertyFactory.class).to(RaoMetaPropertyFactory.class).in(Scopes.SINGLETON);

	bind(EntityFactory.class).toInstance(entityFactory);
    }

    public DomainValidationConfig getDomainValidationConfig() {
	return domainValidationConfig;
    }

    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
	return domainMetaPropertyConfig;
    }

}
