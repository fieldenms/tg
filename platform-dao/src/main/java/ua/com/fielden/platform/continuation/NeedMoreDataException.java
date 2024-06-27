package ua.com.fielden.platform.continuation;

import ua.com.fielden.platform.entity.IContinuationData;

import java.util.Optional;

/**
 * The exception type that is used as part of the continuation handling implementation.
 * Its main purpose is to capture the data needed to continue the computation is was thrown from.
 * 
 * @author TG Team
 *
 */
public class NeedMoreDataException extends RuntimeException {
    public static final String MSG_STANDARD = "Continuation for [%s] entity and property [%s].";

    public final Class<? extends IContinuationData> continuationType;
    public final Optional<? extends IContinuationData> maybeContinuation;
    public final String continuationTypeStr;
    public final String continuationProperty;

    /**
     * Creates an exception with an initialised instance of the data needed or its type, a field name that it should be associated with, and a custom error message.
     *
     * @param customMessage         a custom message to be displayed in Web UI as a toast.
     * @param continuationType      an action-entity type that represents a continuation.
     * @param maybeContinuation     an initialised action-entity instance that represent a continuation.
     * @param continuationProperty  a field name of a companion object to which a continuation instance is going to be assigned to.
     * @param <T>
     */
    private <T extends IContinuationData> NeedMoreDataException(final String customMessage, final Class<? extends T> continuationType, final Optional<T> maybeContinuation, final String continuationProperty) {
        super(customMessage);
        this.maybeContinuation = maybeContinuation;
        this.continuationType = continuationType;
        this.continuationTypeStr = continuationType.getName();
        this.continuationProperty = continuationProperty;
    }

    /**
     * Creates an exception with a type of continuation.
     *
     * @param customMessage         a custom message to be displayed in Web UI as a toast.
     * @param continuationType      an action-entity type that represents a continuation.
     * @param continuationProperty  a field name of a companion object to which a continuation instance is going to be assigned to.
     */
    public NeedMoreDataException(final String customMessage, final Class<? extends IContinuationData> continuationType, final String continuationProperty) {
        this(customMessage, continuationType, Optional.empty(), continuationProperty);
    }

    /**
     * The same as {@link #NeedMoreDataException(String, Class, String)}, but with a standard message.
     *
     * @param continuationType
     * @param continuationProperty
     */
    public NeedMoreDataException(final Class<? extends IContinuationData> continuationType, final String continuationProperty) {
        this(MSG_STANDARD.formatted(continuationType.getSimpleName(), continuationProperty), continuationType, Optional.empty(), continuationProperty);
    }


    /**
     * Creates an exception with an instance of {@code continuation}.
     *
     * @param customMessage         a custom message to be displayed in Web UI as a toast.
     * @param continuation          an initialised action-entity instance that represents a continuation.
     * @param continuationProperty  a field name of a companion object to which a continuation instance is going to be assigned to.
     */
    public <T extends IContinuationData> NeedMoreDataException(final String customMessage, final T continuation, final String continuationProperty) {
        this(customMessage, continuation.getClass(), Optional.of(continuation), continuationProperty);
    }

    /**
     * The same as {@link #NeedMoreDataException(String, IContinuationData, String)}, but with a standard message.
     * @param continuation
     * @param continuationProperty
     * @param <T>
     */
    public <T extends IContinuationData> NeedMoreDataException(final T continuation, final String continuationProperty) {
        this(MSG_STANDARD.formatted(continuation, continuationProperty), continuation.getClass(), Optional.of(continuation), continuationProperty);
    }

}
