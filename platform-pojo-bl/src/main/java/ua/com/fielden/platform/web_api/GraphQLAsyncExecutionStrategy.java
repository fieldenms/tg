package ua.com.fielden.platform.web_api;

import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * A standard graphql-java {@link AsyncExecutionStrategy} with exception logging.
 * Method {@link AsyncExecutionStrategy#resolveFieldWithInfo(ExecutionContext, ExecutionStrategyParameters)} covers exceptions that are not covered during the "value fetching" phase using 'exceptionHandler'.
 * This mainly pertains to exceptions that may get thrown during the "value completion" phase (e.g., when scalars get coerced).
 *
 * @author TG Team
 */
public class GraphQLAsyncExecutionStrategy extends AsyncExecutionStrategy {
    private static final Logger LOGGER = getLogger(GraphQLAsyncExecutionStrategy.class);

    /**
     * A standard GraphQL execution strategy that processes fields asynchronously.
     */
    public GraphQLAsyncExecutionStrategy() {
        super();
    }

    /**
     * Creates an execution strategy that uses the provided exception handler.
     *
     * @param exceptionHandler an exception handler.
     */
    public GraphQLAsyncExecutionStrategy(final DataFetcherExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    protected Object resolveFieldWithInfo(final ExecutionContext executionContext, final ExecutionStrategyParameters parameters) {
        final var result = super.resolveFieldWithInfo(executionContext, parameters);
        if (result instanceof CompletableFuture<?> cf) {
            cf.whenComplete((r, ex) -> {
                if (ex != null) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            });
        }
        return result;
    }

}
