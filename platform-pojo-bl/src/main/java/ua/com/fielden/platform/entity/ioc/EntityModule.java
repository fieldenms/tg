package ua.com.fielden.platform.entity.ioc;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.subclassesOf;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.security.AuthorisationInterceptor;
import ua.com.fielden.platform.security.Authorise;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * This Guice module ensures that properties for all {@link AbstractEntity} descendants are provided with an intercepter handling validation and observation.
 *
 * @author TG Team
 */
public abstract class EntityModule extends AbstractModule implements IModuleWithInjector {

    private final AuthorisationInterceptor ai = new AuthorisationInterceptor();

    /**
     * Binds intercepter for observable property mutators to ensure property change observation and validation. Only descendants of {@link AbstractEntity} are processed.
     */
    @Override
    protected void configure() {
	// observable interceptor
	bindInterceptor(subclassesOf(AbstractEntity.class), // match {@link AbstractEntity} descendants only
		annotatedWith(Observable.class), // having annotated methods
		new ObservableMutatorInterceptor()); // the intercepter

	bindInterceptor(any(), // match any class
		annotatedWith(Authorise.class), // having annotated methods
		ai); // the intercepter
    }

    @Override
    public void setInjector(final Injector injector) {
	ai.setInjector(injector);
    }
}
