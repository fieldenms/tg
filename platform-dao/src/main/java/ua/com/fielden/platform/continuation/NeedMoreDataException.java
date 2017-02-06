package ua.com.fielden.platform.continuation;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.IContinuationData;

/**
 * The exception type that is used as part of the continuation handling implementation.
 * Its main purpose is to capture the data needed to continue the computation is was thrown from.
 * 
 * @author TG Team
 *
 */
public class NeedMoreDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public final String continuationTypeStr;
    public final String continuationProperty;

    /**
     * Creates exception based on the type of data needed, a key that it should be associated with and a custom error message.
     *
     * @param customMessage -- custom exception message to be shown in toast
     * @param continuationType -- functional entity type that represents continuation
     * @param continuationProperty -- the property on companion object into which continuation will arrive
     */
    NeedMoreDataException(final String customMessage, final Class<? extends IContinuationData> continuationType, final String continuationProperty) {
        super(customMessage);
        this.continuationTypeStr = continuationType.getName();
        this.continuationProperty = continuationProperty;
    }
    
    /**
     * Creates exception based on data type and key.
     *
     * @param continuationType -- functional entity type that represents continuation
     * @param continuationProperty -- the property on companion object into which continuation will arrive
     */
    NeedMoreDataException(final Class<? extends IContinuationData> continuationType, final String continuationProperty) {
        this(format("Continuation for [%s] entity and property [%s].", continuationType.getSimpleName(), continuationProperty), continuationType, continuationProperty);
    }
}
