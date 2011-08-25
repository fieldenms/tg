package ua.com.fielden.platform.test;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.keygen.KeyNumber;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.EntityWithExTaxAndTaxMoney;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleTaxMoney;
import ua.com.fielden.platform.persistence.types.EntityWithTaxMoney;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.domain.entities.Advice;
import ua.com.fielden.platform.test.domain.entities.AdvicePosition;
import ua.com.fielden.platform.test.entities.ComplexKeyEntity;
import ua.com.fielden.platform.test.entities.CompositeEntity;
import ua.com.fielden.platform.test.entities.CompositeEntityKey;
import ua.com.fielden.platform.test.entities.meta.AdviceDispatchedToWorkshopMetaDefiner;
import ua.com.fielden.platform.test.entities.meta.AdviceRoadMetaDefiner;
import ua.com.fielden.platform.test.entities.validators.AdviceCarrierValidator;
import ua.com.fielden.platform.test.entities.validators.AdvicePositionRotableValidator;
import ua.com.fielden.platform.test.entities.validators.AdviceRoadValidator;
import ua.com.fielden.platform.test.ioc.DaoTestHibernateModule;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Provides platform specific implementation of {@link IDbDrivenTestCaseConfiguration}, which is mainly related to the use of {@link DaoTestHibernateModule}.
 *
 * @author TG Team
 *
 */
public class PlatformDbDrivenTestCaseConfiguration implements IDbDrivenTestCaseConfiguration {
    protected final EntityFactory entityFactory;
    protected final Injector injector;
    protected final HibernateUtil hibernateUtil;

    private final DaoTestHibernateModule hibernateModule;

    private final Class<?>[] testDomain = { CompositeEntity.class, CompositeEntityKey.class, ComplexKeyEntity.class, //
	    EntityCentreConfig.class, EntityMasterConfig.class, EntityLocatorConfig.class, //
	    MainMenuItem.class, MainMenuItemInvisibility.class, KeyNumber.class, User.class, UserRole.class, //
	    UserAndRoleAssociation.class, SecurityRoleAssociation.class, EntityWithMoney.class, EntityWithTaxMoney.class, //
	    EntityWithExTaxAndTaxMoney.class, EntityWithSimpleTaxMoney.class, EntityWithSimpleMoney.class, EntityWithDynamicCompositeKey.class };

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();

    static {
	hibTypeDefaults.put(boolean.class, YesNoType.class);
	hibTypeDefaults.put(Boolean.class, YesNoType.class);
	hibTypeDefaults.put(Date.class, DateTimeType.class);
    }


    /**
     * Required for dynamic instantiation by {@link DbDrivenTestCase}
     */
    public PlatformDbDrivenTestCaseConfiguration() {
	// instantiate all the factories and Hibernate utility
	final ProxyInterceptor interceptor = new ProxyInterceptor();
	try {
	    final MappingsGenerator mappingsGenerator = new MappingsGenerator(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()));
	    final Configuration cfg = new Configuration();
	    cfg.addXML(mappingsGenerator.generateMappings(testDomain));

	    hibernateUtil = new HibernateUtil(interceptor, cfg.configure(new URL("file:src/test/resources/hibernate4test.cfg.xml")));
	    hibernateModule = new DaoTestHibernateModule(hibernateUtil.getSessionFactory(), new MappingExtractor(hibernateUtil.getConfiguration()), mappingsGenerator);
	    injector = new ApplicationInjectorFactory().add(hibernateModule).getInjector();
	    entityFactory = injector.getInstance(EntityFactory.class);
	    interceptor.setFactory(entityFactory);

	    // bind domain specific validation classes
	    hibernateModule.getDomainValidationConfig().setValidator(Advice.class, "road", new AdviceRoadValidator()).setValidator(Advice.class, "carrier", injector.getInstance(AdviceCarrierValidator.class)).setValidator(AdvicePosition.class, "rotable", new AdvicePositionRotableValidator());
	    // bind domain specific meta property configuration classes
	    hibernateModule.getDomainMetaPropertyConfig().setDefiner(Advice.class, "dispatchedToWorkshop", new AdviceDispatchedToWorkshopMetaDefiner()).setDefiner(Advice.class, "road", new AdviceRoadMetaDefiner());
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
