package ua.com.fielden.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.MoneyUserType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.IDbDrivenTestCaseConfiguration;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.web.entities.InspectedEntity;

/**
 * Provides a test specific implementation of {@link IDbDrivenTestCaseConfiguration}.
 *
 * @author TG Team
 *
 */
public class PlatformWebDbDrivenTestCaseConfiguration implements IDbDrivenTestCaseConfiguration {
    protected final EntityFactory entityFactory;
    protected final Injector injector;
    protected final HibernateUtil hibernateUtil;

    private final WebHibernateModule hibernateModule;

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();

    static {
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, MoneyUserType.class);
    }

    private final ISerialisationClassProvider serialisationClassProvider = new ProvidedSerialisationClassProvider(new Class[] { InspectedEntity.class });

    /**
     * Required for dynamic instantiation by {@link DbDrivenTestCase}
     */
    public PlatformWebDbDrivenTestCaseConfiguration() {
        // instantiate all the factories and Hibernate utility
        final ProxyInterceptor interceptor = new ProxyInterceptor();
        try {

            final Configuration cfg = new Configuration();
            final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();
            domainTypes.add(User.class);
            domainTypes.add(UserRole.class);
            domainTypes.add(UserAndRoleAssociation.class);
            domainTypes.add(UserAndRoleAssociationBatchAction.class);
            domainTypes.add(SecurityRoleAssociation.class);
            domainTypes.add(SecurityRoleAssociationBatchAction.class);
            domainTypes.add(InspectedEntity.class);
            domainTypes.add(Attachment.class);
            final DomainMetadata domainMetadata = new DomainMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()), domainTypes, AnnotationReflector.getAnnotation(User.class, MapEntityTo.class), DbVersion.H2);
            cfg.addXML(new HibernateMappingsGenerator().generateMappings(domainMetadata));

            cfg.setProperty("hibernate.current_session_context_class", "thread");
            cfg.setProperty("hibernate.show_sql", "false");
            cfg.setProperty("hibernate.format_sql", "true");
            cfg.setProperty("hibernate.connection.url", "jdbc:h2:./src/test/resources/db/testdb");
            cfg.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            cfg.setProperty("hibernate.connection.username", "sa");
            cfg.setProperty("hibernate.connection.password", "");

            hibernateUtil = new HibernateUtil(interceptor, cfg);
            hibernateModule = new WebHibernateModule(hibernateUtil.getSessionFactory(), domainMetadata, serialisationClassProvider);
            injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
            entityFactory = injector.getInstance(EntityFactory.class);
            interceptor.setFactory(entityFactory);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public HibernateUtil getHibernateUtil() {
        return hibernateUtil;
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    @Override
    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
        return hibernateModule.getDomainMetaPropertyConfig();
    }

    @Override
    public DomainValidationConfig getDomainValidationConfig() {
        return hibernateModule.getDomainValidationConfig();
    }

    @Override
    public List<String> getDdl() {
        return null;
    }
}
