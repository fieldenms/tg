package ua.com.fielden.platform.ioc;

import com.google.inject.AbstractModule;
import jakarta.inject.Inject;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsVerifier;

/// Verifies that no entity type has cyclic dependencies between its calculated properties.
///
/// This service can be installed in an IoC module using [AbstractModule#requestStaticInjection].
///
final class DependentCalcPropsVerificationService {

    @Inject
    static void start(final QuerySourceInfoProvider querySourceInfoProvider, final DependentCalcPropsVerifier verifier) {
        verifier.verify(querySourceInfoProvider.modelledQuerySourceInfos());
    }

    private DependentCalcPropsVerificationService() {}

}
