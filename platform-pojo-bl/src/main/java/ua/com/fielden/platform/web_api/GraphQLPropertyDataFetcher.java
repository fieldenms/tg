package ua.com.fielden.platform.web_api;

import graphql.PublicApi;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.PropertyDataFetcher;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;

import static graphql.Assert.assertNotNull;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * This is a default {@link DataFetcher} used in TG Web API implementation. It is similar to graphql-java's {@link PropertyDataFetcher}, but only considers entities as the source.
 * <p>
 * For a root GraphQL field fetcher, that actually fetches a complete data graph using EQL, see {@link RootEntityFetcher}.
 *
 * @author TG Team
 */
@PublicApi
public class GraphQLPropertyDataFetcher<T> implements DataFetcher<T> {
    private static final Logger LOGGER = getLogger(GraphQLPropertyDataFetcher.class);
    private static final String WARN_UNSUPPORTED_PARENT_OBJECT = "Value resolving for property [%s] failed. Parent object [%s] is not of entity type.";
    private final String propertyName;

    /**
     * This constructor accepts a property name, which should not be {@code null}.
     *
     * @param propertyName a name of the property to retrieve.
     */
    public GraphQLPropertyDataFetcher(final String propertyName) {
        this.propertyName = assertNotNull(propertyName);
    }

    /**
     * Returns a data fetcher for property name that examines a {@link DataFetchingEnvironment#getSource()} object (entity) using {@link AbstractEntity#get(String)} method.
     * <p>
     * For example :
     * <pre>
     * {@code
     *
     *      DataFetcher functionDataFetcher = fetching("entityPropertyName");
     *
     * }
     * </pre>
     *
     * @param propertyName a name of the property to retrieve
     * @param <T>          a type of result
     *
     * @return a new {@link DataFetcher} for {@code propertyName} as its source of values
     */
    public static <T> GraphQLPropertyDataFetcher<T> fetching(String propertyName) {
        return new GraphQLPropertyDataFetcher<>(propertyName);
    }

    @Override
    public T get(final DataFetchingEnvironment environment) {
        return (T) getPropertyValue(propertyName, environment.getSource());
    }

    public static Object getPropertyValue(final String propertyName, final Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof AbstractEntity entity) {
            return entity.get(propertyName);
        }
        LOGGER.warn(WARN_UNSUPPORTED_PARENT_OBJECT.formatted(propertyName, object));
        return null; // default "unresolved" value
    }

}
