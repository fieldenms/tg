package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import ua.com.fielden.platform.basic.config.Workflows;

import static java.util.Objects.requireNonNull;

public final class DynamicPropertyAccessModule extends AbstractModule {

    public static final class Options extends AbstractModule {

        private final Caching caching;

        public enum Caching { ENABLED, DISABLED, AUTO }

        public Options(final Caching caching) {
            requireNonNull(caching);
            this.caching = caching;
        }

        @Override
        protected void configure() {
            bind(Options.class).toInstance(this);
        }

        /**
         * Explicitly controls the use of caching, bypassing the standard choice based on the active {@linkplain Workflows workflow}.
         */
        public Options caching(final Caching value) {
            return new Options(value);
        }
    }

    public static Options options() {
        return new Options(Options.Caching.AUTO);
    }

    private Options getOptions(final Injector injector) {
        if (injector.getExistingBinding(Key.get(Options.class)) != null) {
            return injector.getInstance(Options.class);
        } else {
            return options();
        }
    }

    @Override
    protected void configure() {
        requestStaticInjection(AbstractEntity.class);
    }

    @Provides
    PropertyIndexer providePropertyIndexer(final Workflows workflow, final Injector injector) {
        final Options options = getOptions(injector);
        return switch (options.caching) {
            case ENABLED -> new CachingPropertyIndexerImpl();
            case DISABLED -> new PropertyIndexerImpl();
            case AUTO -> switch (workflow) {
                case deployment, vulcanizing -> new CachingPropertyIndexerImpl();
                /*
                 Caching during development can be enabled if we can guarantee that it won't get in the way of redefining
                 entity types at runtime. So which entity types can be redefined?
                 * Canonical entity types - if HotSpot is used, then no, because it doesn't support structural changes.
                 If JBR (JetBrains Runtime) is used, then canonical entity types can indeed be redefined: properties
                 can be removed, added and modified. However, the implications of such changes are far too wide, they
                 would also affect other parts of the system (e.g., metadata). Therefore, since this isn't supported
                 yet, we need not worry about it here.
                 * Generated entity types - any live changes should result in generation of new entity types (e.g.,
                 modifying an entity centre configuration). Since those types will be new, old cached types won't get
                 in the way.
                */
                case development -> new CachingPropertyIndexerImpl();
            };
        };
    }

}
