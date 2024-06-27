package ua.com.fielden.platform.entity.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import org.aopalliance.intercept.MethodInterceptor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.security.AuthorisationInterceptor;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.web_api.GraphQLScalars;

import java.lang.reflect.Method;

import static com.google.inject.matcher.Matchers.*;

/**
 * This Guice module ensures that properties for all {@link AbstractEntity} descendants are provided with an intercepter handling validation and observation.
 *
 * @author TG Team
 */
public abstract class EntityModule extends AbstractModule {

    /**
     * Synthetic methods should not be intercepted.
     */
    private final AbstractMatcher<Method> noSyntheticMethodMatcher = new AbstractMatcher<Method>() {
        @Override
        public boolean matches(final Method method) {
            return !method.isSynthetic();
        }
    };

    /**
     * Binds intercepter for observable property mutators to ensure property change observation and validation. Only descendants of {@link AbstractEntity} are processed.
     */
    @Override
    protected void configure() {
        // observable interceptor
        bindInterceptor(subclassesOf(AbstractEntity.class), // match {@link AbstractEntity} descendants only
                annotatedWith(Observable.class), // having annotated methods
                new ObservableMutatorInterceptor()); // the interceptor

        // authorisation interceptor
        bindInterceptor(any(), // match any class
                annotatedWith(Authorise.class), // having annotated methods
                new AuthorisationInterceptor(getProvider(IAuthorisationModel.class))); // the interceptor

        // request static IDates injection into GraphQLScalars;
        // static injection occurs at the time when an injector is created
        // this guarantees that different implementations of IDates will be injected based on IDates binding in IoC modules that define the binding configuration;
        requestStaticInjection(GraphQLScalars.class);
    }

    @Override
    protected void bindInterceptor(final Matcher<? super Class<?>> classMatcher, final Matcher<? super Method> methodMatcher, final MethodInterceptor... interceptors) {
        super.bindInterceptor(classMatcher, noSyntheticMethodMatcher.and(methodMatcher), interceptors);
    }

}
