package ua.com.fielden.platform.ioc;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Proxy;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.subclassesOf;

/**
 * Guice injector module for platform-wide Hibernate related injections such as transaction support and domain level validation configurations.
 *
 * @author TG Team
 *
 */
public class TransactionalModule extends EntityModule {
    protected final SessionFactory sessionFactory;
    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();
    private final MappingExtractor mappingExtractor;
    private final MappingsGenerator mappingsGenerator;


    /**
     * Creates transactional module, which holds references to instances of {@link SessionFactory} and {@link MappingExtractor}. All descending classes needs to provide those two parameters.
     * @param sessionFactory
     * @param mappingExtractor
     */
    public TransactionalModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor, final MappingsGenerator mappingsGenerator) {
	this.sessionFactory = sessionFactory;
	this.mappingExtractor = mappingExtractor;
	this.mappingsGenerator = mappingsGenerator;
    }

    @Override
    protected void configure() {
	super.configure();
	// entity aggregates transformer
	bind(MappingExtractor.class).toInstance(mappingExtractor);
	// order of intercepter binding is extremely important as it defines the order of their execution if applied to the same method
	bindInterceptor(any(), // match any class
		annotatedWith(Transactional.class), // having annotated methods
		new TransactionalInterceptor(sessionFactory) // the intercepter
	);
	// bind SessionRequired injector
	bindInterceptor(subclassesOf(CommonEntityDao.class), // match only DAO derived from  CommonEntityDao
		annotatedWith(SessionRequired.class), // having annotated methods
		new SessionInterceptor(sessionFactory) // the intercepter
	);
	// TODO This an experimental support for proxied properties with lazy loading. It can also be used with proxied methods.
	// bind PropertyProxyInterseptor
	bindInterceptor(subclassesOf(AbstractEntity.class), // match only entity classes
		annotatedWith(Proxy.class), // having annotated methods
		new PropertyProxyInterceptor(sessionFactory) // the intercepter
	);
	// bind DomainValidationConfig
	bind(DomainValidationConfig.class).toInstance(domainValidationConfig);
	// bind DomainMetaPropertyConfig
	bind(DomainMetaPropertyConfig.class).toInstance(domainMetaPropertyConfig);
    }

    public DomainValidationConfig getDomainValidationConfig() {
	return domainValidationConfig;
    }

    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
	return domainMetaPropertyConfig;
    }

    protected MappingExtractor getMappingExtractor() {
	return mappingExtractor;
    }

    public MappingsGenerator getMappingsGenerator() {
        return mappingsGenerator;
    }
}
