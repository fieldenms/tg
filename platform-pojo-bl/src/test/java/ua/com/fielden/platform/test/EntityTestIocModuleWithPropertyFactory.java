package ua.com.fielden.platform.test;

import ua.com.fielden.platform.companion.IEntityCompanionGenerator;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.CanBuildReferenceHierarchyForEveryEntityValidator;
import ua.com.fielden.platform.entity.validation.ICanBuildReferenceHierarchyForEntityValidator;
import ua.com.fielden.platform.ioc.EntityIocModule;
import ua.com.fielden.platform.ref_hierarchy.IReferenceHierarchy;
import ua.com.fielden.platform.sample.domain.ReferenceHierarchyDaoStub;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.utils.IDates;

import java.util.Properties;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityIocModule}, this module binds
 * {@link IMetaPropertyFactory}.
 * 
 * IMPORTANT: This module is applicable strictly for testing purposes! Left in the main source (e.i. not test) due to the need to be visible in other projects.
 * 
 * @author TG Team
 */
public class EntityTestIocModuleWithPropertyFactory extends EntityIocModule {

    public EntityTestIocModuleWithPropertyFactory() {
        super();
    }

    public EntityTestIocModuleWithPropertyFactory(final Properties properties) {
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
        bind(IMetaPropertyFactory.class).to(TestMetaPropertyFactory.class);

        bind(IReferenceHierarchy.class).to(ReferenceHierarchyDaoStub.class);
        bind(ICanBuildReferenceHierarchyForEntityValidator.class).to(CanBuildReferenceHierarchyForEveryEntityValidator.class);
        bind(IDates.class).to(DatesForTesting.class);

        bind(IEntityCompanionGenerator.class).to(EntityCompanionGeneratorStub.class);
    }

}
