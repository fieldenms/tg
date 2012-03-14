package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Proxy;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.subclassesOf;

/**
 * Guice injector module for platform-wide Hibernate related injections such as transaction support and domain level validation configurations.
 *
 * @author TG Team
 *
 */
public abstract class TransactionalModule2 extends EntityModule {
    protected final SessionFactory sessionFactory;
    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();
    private final MappingsGenerator mappingsGenerator;
    protected final ProxyInterceptor interceptor;
    private final HibernateUtil hibernateUtil;

    /**
     * Creates transactional module, which holds references to instances of {@link SessionFactory} and {@link MappingsGenerator}. All descending classes needs to provide those two
     * parameters.
     *
     * @param sessionFactory
     * @param mappingExtractor
     * @throws Exception
     */
    public TransactionalModule2(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity>> applicationEntityTypes) throws Exception {
	final HibernateConfigurationFactory hcf = new HibernateConfigurationFactory(props, defaultHibernateTypes, applicationEntityTypes);
	final Configuration hibernateConfig = hcf.build();
	interceptor = new ProxyInterceptor();
	hibernateUtil = new HibernateUtil(interceptor, hibernateConfig);

	this.sessionFactory = hibernateUtil.getSessionFactory();
	this.mappingsGenerator = hcf.getMappingsGenerator();
    }

    public TransactionalModule2(final SessionFactory sessionFactory, final MappingsGenerator mappingsGenerator) {
	interceptor = null;
	hibernateUtil = null;

	this.sessionFactory = sessionFactory;
	this.mappingsGenerator = mappingsGenerator;
    }

    @Override
    protected void configure() {
	super.configure();
	// entity aggregates transformer
	if (mappingsGenerator != null) {
	    bind(MappingsGenerator.class).toInstance(mappingsGenerator);
	}
	// hibernate util
	if (hibernateUtil != null) {
	    bind(HibernateUtil.class).toInstance(hibernateUtil);
	}

	// order of intercepter binding is extremely important as it defines the order of their execution if applied to the same method
	bindInterceptor(any(), // match any class
	annotatedWith(Transactional.class), // having annotated methods
	new TransactionalInterceptor(sessionFactory) // the intercepter
	);
	// bind SessionRequired injector
	bindInterceptor(subclassesOf(CommonEntityDao2.class), // match only DAO derived from  CommonEntityDao
	annotatedWith(SessionRequired.class), // having annotated methods
	new SessionInterceptor2(sessionFactory) // the intercepter
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

    public MappingsGenerator getMappingsGenerator() {
	return mappingsGenerator;
    }
}