package ua.com.fielden.benchmark;

import com.google.inject.name.Names;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.ioc.EntityIocModule;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityIocModule}, this module binds {@link IMetaPropertyFactory}.
 * <p>
 * This was a result of EntityModuleWithPropertyFactory merging with CommonTestEntityModuleWithPropertyFactory (they both reside in src/test/java and can not be used).
 * 
 * @author TG Team
 */
public class EntityModuleWithPropertyFactoryForBenchmarking extends EntityIocModule {

    public EntityModuleWithPropertyFactoryForBenchmarking() {}

    public EntityModuleWithPropertyFactoryForBenchmarking(final Properties properties) {
        super(properties);
    }

    /**
     * 
     * Please note that order of validator execution is also defined by the order of binding.
     */
    @Override
    protected void configure() {
        super.configure();
        //////////////////////////////////////////////
        //////////// bind property factory ///////////
        //////////////////////////////////////////////
        bind(IMetaPropertyFactory.class).to(MetaPropertyFactoryForBenchmarking.class);
        
        bindConstant().annotatedWith(Names.named("app.name")).to("Unit Tests");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("192.168.1.8");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tests@tg.org"); 
        
        bind(IApplicationDomainProvider.class).to(ApplicationDomain.class);
        bind(IDates.class).to(DatesForBenchmarking.class);
        bind(IUniversalConstants.class).to(UniversalConstantsForBenchmarking.class);
    }

}
