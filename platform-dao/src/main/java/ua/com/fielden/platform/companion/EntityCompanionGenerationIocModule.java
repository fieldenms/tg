package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

/// This IoC module configures the generation of entity companions.
///
/// @see IEntityCompanionGenerator
///
public final class EntityCompanionGenerationIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(IEntityCompanionGenerator.class).to(EntityCompanionGeneratorImpl.class);
    }

}
