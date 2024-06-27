package ua.com.fielden.deserialisation;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.ioc.IModuleWithInjector;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds {@link IMetaPropertyFactory}.
 * <p>
 * This was a result of EntityModuleWithPropertyFactory merging with CommonTestEntityModuleWithPropertyFactory (they both reside in src/test/java and can not be used).
 * 
 * @author TG Team
 */
class EntityModuleWithPropertyFactoryForBenchmarking extends EntityModule implements IModuleWithInjector {

    protected final EntityFactory entityFactory;

    public EntityModuleWithPropertyFactoryForBenchmarking() {
        entityFactory = new EntityFactory() {
        };
    }

    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();

    /**
     * 
     * Please note that order of validator execution is also defined by the order of binding.
     */
    @Override
    protected void configure() {
        super.configure();
        bind(EntityFactory.class).toInstance(entityFactory);
        //////////////////////////////////////////////
        //////////// bind property factory ///////////
        //////////////////////////////////////////////
        bind(IMetaPropertyFactory.class).toInstance(new AbstractMetaPropertyFactory(domainValidationConfig, domainMetaPropertyConfig, new DatesForBenchmarking()) {

            @Override
            protected IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
                return new HappyValidator();
            }

        });
        
        bindConstant().annotatedWith(Names.named("app.name")).to("Unit Tests");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("192.168.1.8");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tests@tg.org"); 
        
        bind(IApplicationDomainProvider.class).to(ApplicationDomain.class);
        bind(IDates.class).to(DatesForBenchmarking.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForBenchmarking.class).in(Scopes.SINGLETON);
    }

    public DomainValidationConfig getDomainValidationConfig() {
        return domainValidationConfig;
    }

    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
        return domainMetaPropertyConfig;
    }

    @Override
    public void setInjector(final Injector injector) {
        entityFactory.setInjector(injector);
        final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
        mfp.setInjector(injector);
    }

}
