package ua.com.fielden.platform.ioc;

import com.google.inject.AbstractModule;
import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.minheritance.MultiInheritanceEntityVerifier;

/// Performs verification of all generated multi-inheritance entity types that are registered in the application domain.
///
/// This service can be installed in an IoC module using [AbstractModule#requestStaticInjection].
///
final class MultiInheritanceEntityVerificationService {

    @Inject
    static void start(final IApplicationDomainProvider appDomainProvider, final MultiInheritanceEntityVerifier verifier) {
        verifier.verify(appDomainProvider.entityTypes());
    }


    private MultiInheritanceEntityVerificationService() {}

}
