package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import ua.com.fielden.platform.basic.config.Workflows;

import javax.annotation.Nullable;

public final class DynamicPropertyAccessModule extends AbstractModule {

    public static final String ENABLE_CACHE = "DynamicPropertyAcesss.enableCache";

    static class Options {
        /**
         * Explicitly controls the use of caching, bypassing the standard choice based on the active {@linkplain Workflows workflow}.
         */
        @Inject(optional = true)
        @Named(ENABLE_CACHE)
        @Nullable Boolean enableCache = null;
    }

    public static Module enableCache(final boolean value) {
        return binder -> binder.bindConstant().annotatedWith(Names.named(ENABLE_CACHE)).to(value);
    }

    @Override
    protected void configure() {
        requestStaticInjection(AbstractEntity.class);
    }

    @Provides
    PropertyIndexer providePropertyIndexer(final Workflows workflow, final Options options) {
        final boolean caching;
        if (options.enableCache != null) {
            caching = options.enableCache;
        }
        else {
            caching = switch (workflow) {
                case deployment, vulcanizing -> true;
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
                case development -> true;
            };
        }

        return caching ? new CachingPropertyIndexerImpl() : new PropertyIndexerImpl();
    }

}
