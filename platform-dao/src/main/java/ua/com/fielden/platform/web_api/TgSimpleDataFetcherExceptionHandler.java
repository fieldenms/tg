package ua.com.fielden.platform.web_api;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standard graphql-java {@link SimpleDataFetcherExceptionHandler} with exception logging.
 */
public class TgSimpleDataFetcherExceptionHandler extends SimpleDataFetcherExceptionHandler {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    protected void logException(ExceptionWhileDataFetching error, Throwable exception) {
        super.logException(error, exception);
        logger.error(exception.getMessage(), exception);
    }

}
