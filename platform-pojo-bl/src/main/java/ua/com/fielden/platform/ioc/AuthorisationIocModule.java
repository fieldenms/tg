package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.security.AuthorisationInterceptor;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

public final class AuthorisationIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        // authorisation interceptor
        bindInterceptor(any(), // match any class
                        annotatedWith(Authorise.class), // having methods that require authorisation
                        new AuthorisationInterceptor(getProvider(IAuthorisationModel.class))); // the interceptor

    }

}
