package ua.com.fielden.platform.test;

import com.google.inject.Scopes;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 * 
 * IMPORTANT: This module is applicable strictly for testing purposes! Left in the main source (e.i. not test) due to the need to be visible in other projects.
 * 
 * @author TG Team
 */
public final class CommonTestEntityModuleWithPropertyFactory extends EntityModuleWithPropertyFactory {

    @Override
    protected void configure() {
        super.configure();
        
        bind(IApplicationDomainProvider.class).to(ApplicationDomain.class);
        bind(ICompanionObjectFinder.class).to(DefaultCompanionObjectFinderImpl.class).in(Scopes.SINGLETON);
    }
    
}
