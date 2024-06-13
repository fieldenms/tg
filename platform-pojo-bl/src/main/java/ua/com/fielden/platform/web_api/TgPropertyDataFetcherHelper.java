package ua.com.fielden.platform.web_api;

import java.util.function.Supplier;

import graphql.Internal;
import graphql.VisibleForTesting;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLType;

/**
 * This class is the guts of a property data fetcher and also used in AST code to turn
 * in memory java objects into AST elements
 */
@Internal
public class TgPropertyDataFetcherHelper {

    private static final TgPropertyFetchingImpl impl = new TgPropertyFetchingImpl(DataFetchingEnvironment.class);

    public static Object getPropertyValue(final String propertyName, final Object object, final GraphQLType graphQLType) {
        return impl.getPropertyValue(propertyName, object, graphQLType, false, () -> null);
    }

    public static Object getPropertyValue(final String propertyName, final Object object, final GraphQLType graphQLType, final Supplier<DataFetchingEnvironment> environment) {
        return impl.getPropertyValue(propertyName, object, graphQLType, true, environment::get);
    }

    public static void clearReflectionCache() {
        impl.clearReflectionCache();
    }

    public static boolean setUseSetAccessible(final boolean flag) {
        return impl.setUseSetAccessible(flag);
    }

    @VisibleForTesting
    public static boolean setUseLambdaFactory(final boolean flag) {
        return impl.setUseLambdaFactory(flag);
    }

    public static boolean setUseNegativeCache(final boolean flag) {
        return impl.setUseNegativeCache(flag);
    }
}