package ua.com.fielden.platform.entity.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import org.aopalliance.intercept.MethodInterceptor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicPropertyAccessModule;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.web_api.GraphQLScalars;

import java.lang.reflect.Method;
import java.util.Properties;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static ua.com.fielden.platform.ioc.Matchers.notSyntheticMethod;

/**
 * This Guice module ensures that properties for all {@link AbstractEntity} descendants are provided with an intercepter handling validation and observation.
 *
 * @author TG Team
 */
public abstract class EntityModule extends AbstractModule {

    private final Properties properties;

    public EntityModule(final Properties properties) {
        this.properties = properties;
    }

    public EntityModule() {
        this(new Properties());
    }

    /**
     * Binds intercepter for observable property mutators to ensure property change observation and validation. Only descendants of {@link AbstractEntity} are processed.
     */
    @Override
    protected void configure() {
        // observable interceptor
        bindInterceptor(subclassesOf(AbstractEntity.class), // match {@link AbstractEntity} descendants only
                annotatedWith(Observable.class), // having annotated methods
                new ObservableMutatorInterceptor()); // the interceptor

        // request static IDates injection into GraphQLScalars;
        // static injection occurs at the time when an injector is created
        // this guarantees that different implementations of IDates will be injected based on IDates binding in IoC modules that define the binding configuration;
        requestStaticInjection(GraphQLScalars.class);

        install(DynamicPropertyAccessModule.options().fromProperties(properties));
        install(new DynamicPropertyAccessModule());
    }

    @Override
    protected void bindInterceptor(final Matcher<? super Class<?>> classMatcher, final Matcher<? super Method> methodMatcher, final MethodInterceptor... interceptors) {
        super.bindInterceptor(classMatcher, notSyntheticMethod().and(methodMatcher), interceptors);
    }

}
