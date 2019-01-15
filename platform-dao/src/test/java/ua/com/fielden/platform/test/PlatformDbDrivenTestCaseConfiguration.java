package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.YesNoType;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.migration.LegacyConnectionModule;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.EntityWithExTaxAndTaxMoney;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleTaxMoney;
import ua.com.fielden.platform.persistence.types.EntityWithTaxMoney;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModelWithCalc;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.entities.ComplexKeyEntity;
import ua.com.fielden.platform.test.entities.CompositeEntity;
import ua.com.fielden.platform.test.entities.CompositeEntityKey;
import ua.com.fielden.platform.test.ioc.DaoTestHibernateModule;
import ua.com.fielden.platform.types.Money;

/**
 * Provides platform specific implementation of {@link IDbDrivenTestCaseConfiguration}, which is mainly related to the use of {@link DaoTestHibernateModule}.
 *
 * @author TG Team
 *
 * @deprecated Use {@link PlatformDomainDrivenTestCaseConfiguration} instead.
 *
 */
@Deprecated
public class PlatformDbDrivenTestCaseConfiguration implements IDbDrivenTestCaseConfiguration {
    protected final EntityFactory entityFactory;
    protected final Injector injector;
    protected final HibernateUtil hibernateUtil;

    private final DaoTestHibernateModule hibernateModule;

    public static final List<Class<? extends AbstractEntity<?>>> testDomain = new ArrayList<Class<? extends AbstractEntity<?>>>();

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();

    static {
        hibTypeDefaults.put(boolean.class, YesNoType.class);
        hibTypeDefaults.put(Boolean.class, YesNoType.class);
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        hibTypeDefaults.put(PropertyDescriptor.class, PropertyDescriptorType.class);
        testDomain.addAll(PlatformDomainTypes.types);
        testDomain.add(CompositeEntity.class);
        testDomain.add(CompositeEntityKey.class);
        testDomain.add(ComplexKeyEntity.class);
        testDomain.add(EntityWithMoney.class);
        testDomain.add(EntityWithTaxMoney.class);
        testDomain.add(EntityWithExTaxAndTaxMoney.class);
        testDomain.add(EntityWithSimpleTaxMoney.class);
        testDomain.add(EntityWithSimpleMoney.class);
        testDomain.add(EntityWithDynamicCompositeKey.class);
        testDomain.add(TgTimesheet.class);
        testDomain.add(TgVehicleMake.class);
        testDomain.add(TgVehicleModel.class);
        testDomain.add(TgVehicleModelWithCalc.class);
        testDomain.add(TgVehicle.class);
        testDomain.add(TgFuelUsage.class);
        testDomain.add(TgFuelType.class);
        testDomain.add(TgOrgUnit1.class);
        testDomain.add(TgOrgUnit2.class);
        testDomain.add(TgOrgUnit3.class);
        testDomain.add(TgOrgUnit4.class);
        testDomain.add(TgOrgUnit5.class);
        testDomain.add(TgWorkOrder.class);
    }

    /**
     * Required for dynamic instantiation by {@link DbDrivenTestCase}
     */
    public PlatformDbDrivenTestCaseConfiguration() {
        // instantiate all the factories and Hibernate utility
        DOMConfigurator.configure("src/test/resources/log4j.xml");

        final ProxyInterceptor interceptor = new ProxyInterceptor();
        try {
            final DomainMetadata domainMetadata = new DomainMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()), testDomain, DbVersion.H2);
            final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = new IdOnlyProxiedEntityTypeCache(domainMetadata);
            final Configuration cfg = new Configuration();
            
            try {
                cfg.addInputStream(new ByteArrayInputStream(new HibernateMappingsGenerator().generateMappings(domainMetadata).getBytes("UTF8")));
            } catch (final MappingException | UnsupportedEncodingException e) {
                throw new HibernateException("Could not add mappings.", e);
            }

            // this is the old and now deprecated configuration than requires for some properties to be overridden
            cfg.configure(new URL("file:src/test/resources/hibernate4test.cfg.xml"));
            // let's override the database name if supplied to support forked execution of old test cases
            final String databaseUri = System.getProperty("legacyTests.databaseUri");
            if (!StringUtils.isEmpty(databaseUri)) {
                final Properties dbProps = new Properties();
                dbProps.setProperty("hibernate.connection.url", format("jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", databaseUri));
                cfg.addProperties(dbProps);
            }
            
            hibernateUtil = new HibernateUtil(interceptor, cfg);
            hibernateModule = new DaoTestHibernateModule(hibernateUtil.getSessionFactory(), domainMetadata, idOnlyProxiedEntityTypeCache);
            injector = new ApplicationInjectorFactory().add(hibernateModule).add(new NewUserNotifierMockBindingModule()).add(new LegacyConnectionModule(new Provider() {
                @Override
                public Object get() {
                    try {
                        final String connectionUrl = "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1";
                        Class.forName("org.h2.Driver");
                        return DriverManager.getConnection(connectionUrl, "sa", "");
                    } catch (final Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            })).getInjector();
            entityFactory = injector.getInstance(EntityFactory.class);
            interceptor.setFactory(entityFactory);

            // bind domain specific validation classes
            //	    hibernateModule.getDomainValidationConfig().setValidator(Advice.class, "road", new AdviceRoadValidator()).setValidator(Advice.class, "carrier", injector.getInstance(AdviceCarrierValidator.class)).setValidator(AdvicePosition.class, "rotable", new AdvicePositionRotableValidator());
            // bind domain specific meta property configuration classes
            //	    hibernateModule.getDomainMetaPropertyConfig().setDefiner(Advice.class, "dispatchedToWorkshop", new AdviceDispatchedToWorkshopMetaDefiner()).setDefiner(Advice.class, "road", new AdviceRoadMetaDefiner());
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