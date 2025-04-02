package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

/**
 * This IoC module configures the generation of audit-entity companions.
 *
 * @see IAuditEntityCompanionGenerator
 */
public final class AuditEntityCompanionGenerationIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(IAuditEntityCompanionGenerator.class).to(AuditEntityCompanionGenerator.class);
    }

}
