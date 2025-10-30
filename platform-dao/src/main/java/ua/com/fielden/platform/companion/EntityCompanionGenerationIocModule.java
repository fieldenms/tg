package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.audit.AuditEntityCompanionGenerationIocModule;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

/// This IoC module configures the generation of entity companions.
///
/// @see ICompanionGenerator
///
public final class EntityCompanionGenerationIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(ICompanionGenerator.class).to(CompanionGeneratorImpl.class);
        install(new AuditEntityCompanionGenerationIocModule());
    }

}
