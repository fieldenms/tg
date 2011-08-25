package ua.com.fielden.platform.entity.ioc;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import ua.com.fielden.platform.security.AuthorisationInterceptor;
import ua.com.fielden.platform.security.Authorise;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * This Guice module provided specifically to support TG authorisation schema in applications that require it.
 *
 * @author TG Team
 */
public class AuthorisationModule extends AbstractModule implements IModuleWithInjector {

    private final AuthorisationInterceptor ai = new AuthorisationInterceptor();

    /**
     * Binds security method interceptor to methods annotated with {@link Authorise}.
     *
     * In general, entity classes would not require authorisation, which is mainly targeted at controllers implementing business logic. However, it is envisaged that there could be
     * situations where a property setter would need to be authorised. In such cases setters would have two method intercepters -- one for authorisation, another for validation and
     * observation. The order of intercepter binding is important -- first should be the authorisation, and only then observable/validation intercepter. This is required for
     * authorisation to take place before any other activity..
     */
    @Override
    protected void configure() {
	// authorisation interceptor
	bindInterceptor(any(), // match any class
	annotatedWith(Authorise.class), // having annotated methods
	ai); // the intercepter
    }

    @Override
    public void setInjector(final Injector injector) {
	ai.setInjector(injector);
    }


}
