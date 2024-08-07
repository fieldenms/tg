package ua.com.fielden.platform.ioc;

import com.google.inject.AbstractModule;
import ua.com.fielden.platform.security.AuthorisationInterceptor;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static ua.com.fielden.platform.ioc.Matchers.notSyntheticMethod;

public final class AuthorisationModule extends AbstractModule {

    @Override
    protected void configure() {
        // authorisation interceptor
        bindInterceptor(any(), // match any class
                        notSyntheticMethod().and(annotatedWith(Authorise.class)), // having annotated methods
                        new AuthorisationInterceptor(getProvider(IAuthorisationModel.class))); // the interceptor

    }

}
