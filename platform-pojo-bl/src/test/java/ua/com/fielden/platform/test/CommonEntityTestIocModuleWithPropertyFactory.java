package ua.com.fielden.platform.test;

import com.google.inject.name.Names;
import ua.com.fielden.platform.audit.AuditingIocModule;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.ioc.EntityIocModule;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesDaoStub;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.util.Properties;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly.
 * In addition to {@link EntityIocModule}, this module binds {@link IMetaPropertyFactory}.
 * This modules disables auditing, but this can be changed by overriding {@link #auditingMode()}.
 * <p>
 * <b>IMPORTANT</b>: This module is strictly for testing purposes!
 * 
 * @author TG Team
 */
public class CommonEntityTestIocModuleWithPropertyFactory extends EntityTestIocModuleWithPropertyFactory {

    public CommonEntityTestIocModuleWithPropertyFactory() {
        super();
    }

    public CommonEntityTestIocModuleWithPropertyFactory(final Properties properties) {
        super(properties);
    }

    protected AuditingMode auditingMode() {
        return AuditingMode.DISABLED;
    }

    @Override
    protected void configure() {
        super.configure();
        
        bindConstant().annotatedWith(Names.named("app.name")).to("Unit Tests");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("192.168.1.8");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tests@tg.org");

        bind(IApplicationDomainProvider.class).to(ApplicationDomain.class);
        bind(IDates.class).to(DatesForTesting.class);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class);
        bind(ITgPersistentEntityWithProperties.class).to(TgPersistentEntityWithPropertiesDaoStub.class);

        install(AuditingIocModule.withAuditingMode(auditingMode()));
    }
    
}
