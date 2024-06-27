package ua.com.fielden.platform.test;

import com.google.inject.Injector;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.ioc.IModuleWithInjector;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.*;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.ref_hierarchy.IReferenceHierarchy;
import ua.com.fielden.platform.sample.domain.ReferenceHierarchyDaoStub;
import ua.com.fielden.platform.test.ioc.DatesForTesting;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 * 
 * IMPORTANT: This module is applicable strictly for testing purposes! Left in the main source (e.i. not test) due to the need to be visible in other projects.
 * 
 * @author TG Team
 */
public class EntityModuleWithPropertyFactory extends EntityModule implements IModuleWithInjector {

    public EntityModuleWithPropertyFactory() {}

    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();

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
        bind(IMetaPropertyFactory.class).toInstance(new AbstractMetaPropertyFactory(domainValidationConfig, domainMetaPropertyConfig, new DatesForTesting()) {

            @Override
            protected IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
                return new HappyValidator();
            }

        });

        bind(IReferenceHierarchy.class).to(ReferenceHierarchyDaoStub.class);
        bind(ICanBuildReferenceHierarchyForEntityValidator.class).to(CanBuildReferenceHierarchyForEveryEntityValidator.class);
    }

    public DomainValidationConfig getDomainValidationConfig() {
        return domainValidationConfig;
    }

    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
        return domainMetaPropertyConfig;
    }

    @Override
    public void setInjector(final Injector injector) {
        final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
        mfp.setInjector(injector);
    }

}
