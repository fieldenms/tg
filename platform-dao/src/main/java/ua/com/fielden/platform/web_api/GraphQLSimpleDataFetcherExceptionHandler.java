package ua.com.fielden.platform.web_api;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import org.apache.logging.log4j.Logger;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Standard graphql-java {@link SimpleDataFetcherExceptionHandler} with exception logging.
 */
public class GraphQLSimpleDataFetcherExceptionHandler extends SimpleDataFetcherExceptionHandler {
    private static final Logger LOGGER = getLogger(GraphQLSimpleDataFetcherExceptionHandler.class);

    @Override
    protected void logException(ExceptionWhileDataFetching error, Throwable exception) {
        super.logException(error, exception);
        LOGGER.error(exception.getMessage(), exception);
    }

}
