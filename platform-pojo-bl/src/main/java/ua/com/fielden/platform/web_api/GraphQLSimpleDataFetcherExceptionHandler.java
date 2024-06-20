package ua.com.fielden.platform.web_api;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import org.apache.logging.log4j.Logger;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * A Standard graphql-java {@link SimpleDataFetcherExceptionHandler} to log exceptions.
 *
 * @author  TG Team
 */
public class GraphQLSimpleDataFetcherExceptionHandler extends SimpleDataFetcherExceptionHandler {
    private static final Logger LOGGER = getLogger(GraphQLSimpleDataFetcherExceptionHandler.class);

    @Override
    protected void logException(ExceptionWhileDataFetching error, Throwable exception) {
        super.logException(error, exception);
        LOGGER.error(exception.getMessage(), exception);
    }

}
