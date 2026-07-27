package ua.com.fielden.platform.test;

import com.google.inject.name.Names;
import ua.com.fielden.platform.companion.ICompanionGenerator;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.CanBuildReferenceHierarchyForEveryEntityValidator;
import ua.com.fielden.platform.entity.validation.ICanBuildReferenceHierarchyForEntityValidator;
import ua.com.fielden.platform.ioc.EntityIocModule;
import ua.com.fielden.platform.ref_hierarchy.IReferenceHierarchy;
import ua.com.fielden.platform.sample.domain.ReferenceHierarchyDaoStub;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.utils.IDates;

import java.util.Properties;

import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_PATH;

/// This Guice module ensures that all observable and validatable properties are handled correctly. In addition to [EntityIocModule],
/// this module binds [IMetaPropertyFactory].
///
public class EntityTestIocModuleWithPropertyFactory extends EntityIocModule {

    public EntityTestIocModuleWithPropertyFactory() {
        super();
    }

    public EntityTestIocModuleWithPropertyFactory(final Properties properties) {
        super(properties);
    }

    /// Please note that order of validator execution is also defined by the order of binding.
    ///
    @Override
    protected void configure() {
        super.configure();

        bind(IMetaPropertyFactory.class).to(TestMetaPropertyFactory.class);

        bind(IReferenceHierarchy.class).to(ReferenceHierarchyDaoStub.class);
        bind(ICanBuildReferenceHierarchyForEntityValidator.class).to(CanBuildReferenceHierarchyForEveryEntityValidator.class);
        bind(IDates.class).to(DatesForTesting.class);

        bind(ICompanionGenerator.class).to(CompanionGeneratorStub.class);

        bindConstant().annotatedWith(Names.named(AUDIT_PATH)).to("../platform-pojo-bl/target/classes");
    }

}
