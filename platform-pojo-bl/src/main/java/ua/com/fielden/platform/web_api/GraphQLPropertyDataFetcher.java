package ua.com.fielden.platform.web_api;

import graphql.PublicApi;
import graphql.schema.*;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.function.Supplier;

import static graphql.Assert.assertNotNull;

/// This is a default [DataFetcher] used in TG Web API implementation.
/// It extends the default [PropertyDataFetcher] with entity-specific handling.
/// When the source is an entity, the property is accessed via [AbstractEntity#get].
///
/// For a root GraphQL field fetcher, that actually fetches a complete data graph using EQL, see [RootEntityFetcher].
///
@PublicApi
public class GraphQLPropertyDataFetcher<T> extends PropertyDataFetcher<T> {

    public GraphQLPropertyDataFetcher(final String propertyName) {
        super(assertNotNull(propertyName));
    }

    /// Creates a data fetcher for the specified property.
    ///
    public static <T> GraphQLPropertyDataFetcher<T> fetching(String propertyName) {
        return new GraphQLPropertyDataFetcher<>(propertyName);
    }

    /// [PropertyDataFetcher] implements [LightDataFetcher], and the execution engine calls this overload rather than [#get(DataFetchingEnvironment)],
    /// which exists to avoid materialising a [DataFetchingEnvironment] for a simple property access.
    ///
    @Override
    public T get(
            final GraphQLFieldDefinition fieldDefinition,
            final Object sourceObject,
            final Supplier<DataFetchingEnvironment> environmentSupplier)
            throws Exception
    {
        if (sourceObject instanceof AbstractEntity<?> entity) {
            return entity.get(getPropertyName());
        }
        else {
            return super.get(fieldDefinition, sourceObject, environmentSupplier);
        }
    }

}
