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
                case development -> false;
            };
        }

        return caching ? new CachingPropertyIndexerImpl() : new PropertyIndexerImpl();
    }

}
