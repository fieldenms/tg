package ua.com.fielden.platform.entity.ioc;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Observable;

import com.google.inject.AbstractModule;

/**
 * This Guice module ensures that properties for all {@link AbstractEntity} descendants are provided with an intercepter handling validation and observation.
 * 
 * @author TG Team
 */
public class EntityModule extends AbstractModule {

    /**
     * Binds intercepter for observable property mutators to ensure property change observation and validation. Only descendants of {@link AbstractEntity} are processed.
     */
    @Override
    protected void configure() {
	// observable interceptor
	bindInterceptor(subclassesOf(AbstractEntity.class), // match {@link AbstractEntity} descendants only
	annotatedWith(Observable.class), // having annotated methods
	new ObservableMutatorInterceptor()); // the intercepter
    }
}
