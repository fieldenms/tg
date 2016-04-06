package ua.com.fielden.platform.test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 * 
 * IMPORTANT: This module is applicable strictly for testing purposes! Left in the main source (e.i. not test) due to the need to be visible in other projects.
 * 
 * @author TG Team
 */
public final class CommonTestEntityModuleWithPropertyFactory extends EntityModuleWithPropertyFactory {

    protected final DefaultCompanionObjectFinderImpl defaultControllerProvider;
    
    public CommonTestEntityModuleWithPropertyFactory() {
        defaultControllerProvider = new DefaultCompanionObjectFinderImpl();
    }

    @Override
    protected void configure() {
        super.configure();
        // bind provider for default entity controller
        bind(ICompanionObjectFinder.class).toInstance(defaultControllerProvider);
    }
    
    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        defaultControllerProvider.setInjector(injector);
    }
}
