package ua.com.fielden.platform.web_api;

import graphql.execution.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Standard graphql-java {@link AsyncExecutionStrategy} with exception logging.
 * Method {@link AsyncExecutionStrategy#resolveFieldWithInfo(ExecutionContext, ExecutionStrategyParameters)} covers other exceptions,
 *  not covered on "value fetching" phase using 'exceptionHandler'. Mostly this contains exceptions during "value completion" phase, e.g. when scalars got coerced.
 */
public class GraphQLAsyncExecutionStrategy extends AsyncExecutionStrategy {
    private final Logger logger = LogManager.getLogger(getClass());

    /**
     * The standard graphql execution strategy that runs fields asynchronously
     */
    public GraphQLAsyncExecutionStrategy() {
        super();
    }

    /**
     * Creates a execution strategy that uses the provided exception handler
     *
     * @param exceptionHandler the exception handler to use
     */
    public GraphQLAsyncExecutionStrategy(DataFetcherExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    protected Object resolveFieldWithInfo(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {
        final var result = super.resolveFieldWithInfo(executionContext, parameters);
        if (result instanceof CompletableFuture<?> cf) {
            cf.whenComplete((r, ex) -> {
                if (ex != null) {
                    logger.error(ex.getMessage(), ex);
                }
            });
        }
        return result;
    }
}
