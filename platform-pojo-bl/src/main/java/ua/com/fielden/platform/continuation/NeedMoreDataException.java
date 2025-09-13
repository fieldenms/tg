package ua.com.fielden.platform.continuation;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.error.Result;

import java.util.Optional;

import static java.util.Optional.of;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;

/// The exception type that is used as part of the continuation handling implementation.
/// Its main purpose is to capture the data needed to continue the computation it was thrown from.
///
public class NeedMoreDataException extends Result {
    public static final String MSG_STANDARD = "Continuation for [%s] entity and property [%s].";
    public static final String CONTINUATION_TYPE_STR = "continuationTypeStr";
    public static final String CONTINUATION_PROPERTY = "continuationProperty";

    public final Class<? extends IContinuationData> continuationType;
    public final Optional<? extends IContinuationData> maybeContinuation;
    public final String continuationTypeStr;
    public final String continuationProperty;

    /// Creates an exception with an initialised instance of the data needed or its type, a field name that it should be associated with, and a custom error message.
    ///
    /// @param customMessage         a custom message to be displayed in Web UI as a toast.
    /// @param continuationType      an action-entity type that represents a continuation.
    /// @param maybeContinuation     an initialised action-entity instance that represent a continuation.
    /// @param continuationProperty  a field name of a companion object to which a continuation instance is going to be assigned to.
    ///
    /// @param <T> the type of continuation functional entity
    ///
    private <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreDataException(
        final String customMessage,
        final Class<T> continuationType,
        final Optional<T> maybeContinuation,
        final String continuationProperty)
    {
        super(maybeContinuation.orElse(null), customMessage);
        this.maybeContinuation = maybeContinuation;
        this.continuationType = (Class<T>) stripIfNeeded(continuationType);
        this.continuationTypeStr = this.continuationType.getName();
        this.continuationProperty = continuationProperty;
    }

    /// Creates an exception with a type of continuation.
    ///
    /// @param customMessage         a custom message to be displayed in Web UI as a toast.
    /// @param continuationType      an action-entity type that represents a continuation.
    /// @param continuationProperty  a field name of a companion object to which a continuation instance is going to be assigned to.
    ///
    /// @param <T> the type of continuation functional entity
    ///
    <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreDataException(
        final String customMessage,
        final Class<T> continuationType,
        final String continuationProperty)
    {
        this(customMessage, continuationType, Optional.empty(), continuationProperty);
    }

    /// The same as [#NeedMoreDataException(String,Class,String)], but with a standard message.
    ///
    /// @param <T> the type of continuation functional entity
    ///
    <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreDataException(
        final Class<T> continuationType,
        final String continuationProperty)
    {
        this(MSG_STANDARD.formatted(continuationType.getSimpleName(), continuationProperty), continuationType, Optional.empty(), continuationProperty);
    }


    /// Creates an exception with an instance of `continuation`.
    ///
    /// @param customMessage         a custom message to be displayed in Web UI as a toast.
    /// @param continuation          an initialised action-entity instance that represents a continuation.
    /// @param continuationProperty  a field name of a companion object to which a continuation instance is going to be assigned to.
    ///
    /// @param <T> the type of continuation functional entity
    ///
    <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreDataException(
        final String customMessage,
        final T continuation,
        final String continuationProperty)
    {
        this(customMessage, (Class<T>) continuation.getClass(), of(continuation), continuationProperty);
    }

    /// The same as [#NeedMoreDataException(String,IContinuationData,String)], but with a standard message.
    ///
    /// @param <T> the type of continuation functional entity
    ///
    <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> NeedMoreDataException(
        final T continuation,
        final String continuationProperty)
    {
        this(MSG_STANDARD.formatted(continuation, continuationProperty), (Class<T>) continuation.getClass(), of(continuation), continuationProperty);
    }

    /// Returns an instance of continuation functional entity in case of explicit usage of instance instead of a type.
    /// Returns empty [Optional] otherwise.
    ///
    /// @param <T> the type of continuation functional entity
    ///
    public <T extends AbstractFunctionalEntityWithCentreContext<?> & IContinuationData> Optional<T> maybeContinuation() {
        return (Optional<T>) maybeContinuation;
    }

}
