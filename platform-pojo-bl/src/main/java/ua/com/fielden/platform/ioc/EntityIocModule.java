package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web_api.GraphQLScalars;

import java.util.Properties;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;

/**
 * This Guice module ensures that properties for all {@link AbstractEntity} descendants are provided with an interceptor for executing BCE and ACE handlers.
 *
 * @author TG Team
 */
public abstract class EntityIocModule extends AbstractPlatformIocModule {

    private final Properties properties;

    public EntityIocModule(final Properties properties) {
        this.properties = properties;
    }

    public EntityIocModule() {
        this(new Properties());
    }

    /**
     * Binds intercepter for observable property mutators to ensure property change observation and validation. Only descendants of {@link AbstractEntity} are processed.
     */
    @Override
    protected void configure() {
        // observable interceptor
        bindInterceptor(subclassesOf(AbstractEntity.class), // match {@link AbstractEntity} descendants only
                annotatedWith(Observable.class), // having observed methods
                new ObservableMutatorInterceptor()); // the interceptor

        // Request static [IDates] injection into [GraphQLScalars] and [EntityUtils].
        // Static injection occurs at the time when an injector is created.
        // This guarantees that different implementations of [IDates] will be injected based on [IDates] binding in IoC modules that define the binding configuration;
        requestStaticInjection(GraphQLScalars.class);
        requestStaticInjection(EntityUtils.class);

        install(DynamicPropertyAccessIocModule.options().fromProperties(properties));
        install(new DynamicPropertyAccessIocModule());
    }

}
