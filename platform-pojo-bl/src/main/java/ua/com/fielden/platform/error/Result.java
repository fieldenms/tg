package ua.com.fielden.platform.error;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a result (an error or success) of some custom logic. That could been the result of some validation or application of some other business rules.
 * <p>
 * Result is considered successful if no exception was specified.
 * <p>
 * Result it self is an exception, and thus can not only be returned as a method result, but also thrown if appropriate.
 *
 * @author TG Team
 *
 */
public class Result extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final String SUCCESSFUL = "Successful";
    private static final String NULL_POINTER_EXCEPTION = "Null pointer exception";
    private static final String EXT_SEPARATOR = "<extended/>";
    private static final String EXT_SEPARATOR_PATTERN = quote(EXT_SEPARATOR);
    private final Exception ex;
    private final String message;
    private final Object instance;

    /**
     * Mainly used for serialisation.
     */
    protected Result() {
        ex = null;
        message = null;
        instance = null;
    }

    private Result(final Object instance, final String message, final Exception exception) {
        this.instance = instance;
        this.message = message;
        this.ex = exception;
    }

    ///////////////////////////////////////////// Successful /////////////////////////////////////////////

    /**
     * Convenient factory method for creating a successful {@link Result}.
     */
    public static Result successful() {
        return successful(null);
    }

    /**
     * Convenient factory method for creating a successful {@link Result} with specified {@code instance}.
     */
    public static Result successful(final Object instance) {
        return new Result(instance, SUCCESSFUL);
    }

    ///////////////////////////////////////////// Informative /////////////////////////////////////////////

    /**
     * Convenient factory method for creating {@link Informative} with specified {@code message}.
     */
    public static Informative informative(final String message) {
        return informative(null, message);
    }

    /**
     * Convenient factory method for creating {@link Informative} with specified {@code message}, formatted with {@code args} using {@link String#format(String, Object...)}.
     */
    public static Informative informativef(final String message, final Object... args) {
        return informative(format(message, args));
    }

    /**
     * Convenient factory method for creating {@link Informative} with specified {@code shortMessage} and {@code extendedMessage}.
     * <p>
     * Use {@code shortMessage} parameter to define concise message, that would nicely fit to short areas.
     * Use {@code extendedMessage} parameter to define more detailed and longer version of {@code shortMessage}. It may have multiple lines and HTML formatting.
     */
    public static Informative informativeEx(final String shortMessage, final String extendedMessage) {
        return informative(shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    /**
     * Convenient factory method for creating {@link Informative} with specified {@code instance} and {@code message}.
     */
    public static Informative informative(final Object instance, final String message) {
        return new Informative(instance, message);
    }

    /**
     * Convenient factory method for creating {@link Informative} with specified {@code instance}, {@code shortMessage} and {@code extendedMessage}.
     * <p>
     * Use {@code shortMessage} parameter to define concise message, that would nicely fit to short areas.
     * Use {@code extendedMessage} parameter to define more detailed and longer version of {@code shortMessage}. It may have multiple lines and HTML formatting.
     */
    public static Informative informativeEx(final Object instance, final String shortMessage, final String extendedMessage) {
        return informative(instance, shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    ///////////////////////////////////////////// Warning /////////////////////////////////////////////

    /**
     * Convenient factory method for creating {@link Warning} with specified {@code message}.
     */
    public static Warning warning(final String message) {
        return warning(null, message);
    }

    /**
     * Convenient factory method for creating {@link Warning} with specified {@code message}, formatted with {@code args} using {@link String#format(String, Object...)}.
     */
    public static Warning warningf(final String message, final Object... args) {
        return warning(format(message, args));
    }

    /**
     * Convenient factory method for creating {@link Warning} with specified {@code shortMessage} and {@code extendedMessage}.
     * <p>
     * Use {@code shortMessage} parameter to define concise message, that would nicely fit to short areas.
     * Use {@code extendedMessage} parameter to define more detailed and longer version of {@code shortMessage}. It may have multiple lines and HTML formatting.
     */
    public static Warning warningEx(final String shortMessage, final String extendedMessage) {
        return warning(shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    /**
     * Convenient factory method for creating {@link Warning} with specified {@code instance} and {@code message}.
     */
    public static Warning warning(final Object instance, final String message) {
        return new Warning(instance, message);
    }

    /**
     * Convenient factory method for creating {@link Warning} with specified {@code instance}, {@code shortMessage} and {@code extendedMessage}.
     * <p>
     * Use {@code shortMessage} parameter to define concise message, that would nicely fit to short areas.
     * Use {@code extendedMessage} parameter to define more detailed and longer version of {@code shortMessage}. It may have multiple lines and HTML formatting.
     */
    public static Warning warningEx(final Object instance, final String shortMessage, final String extendedMessage) {
        return warning(instance, shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    ///////////////////////////////////////////// Failure /////////////////////////////////////////////

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code reason}.
     * Should be used when neither an object in error nor the actual exception type are important.
     * 
     * @param reason -- reason for failure
     */
    public static Result failure(final String reason) {
        return failure(null, reason);
    }

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code reason}, formatted with {@code args} using {@link String#format(String, Object...)}.
     * Should be used when neither an object in error nor the actual exception type are important.
     * 
     * @param reason -- reason for failure
     */
    public static Result failuref(final String reason, final Object... args) {
        return failure(format(reason, args));
    }

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code shortReason} and {@code extendedReason}.
     * Should be used when neither an object in error nor the actual exception type are important.
     * <p>
     * Use {@code shortReason} parameter to define concise reason, that would nicely fit to short areas.
     * Use {@code extendedReason} parameter to define more detailed and longer version of {@code shortReason}. It may have multiple lines and HTML formatting.
     */
    public static Result failureEx(final String shortReason, final String extendedReason) {
        return failure(shortReason + EXT_SEPARATOR + extendedReason);
    }

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code exception}.
     * In some cases there is no need to pass in an instance that is in error -- just an error itself.
     * 
     * @param exception -- associated exception that caused failure
     */
    public static Result failure(final Exception exception) {
        return failure(null, exception);
    }

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code instance} and {@code reason}.
     * Should be used when no particular exception is at fault.
     * 
     * @param instance -- instance that is in error
     * @param reason -- reason for failure
     */
    public static Result failure(final Object instance, final String reason) {
        return failure(instance, new Exception(reason));
    }

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code instance}, {@code shortReason} and {@code extendedReason}.
     * Should be used when no particular exception is at fault.
     * <p>
     * Use {@code shortReason} parameter to define concise reason, that would nicely fit to short areas.
     * Use {@code extendedReason} parameter to define more detailed and longer version of {@code shortReason}. It may have multiple lines and HTML formatting.
     * 
     * @param instance -- instance that is in error
     */
    public static Result failureEx(final Object instance, final String shortReason, final String extendedReason) {
        return failure(instance, shortReason + EXT_SEPARATOR + extendedReason);
    }

    /**
     * Convenient factory method for creating a failure {@link Result} with specified {@code instance} and {@code exception}.
     * 
     * @param instance -- instance that is in error
     * @param exception -- associated exception that caused failure
     */
    public static Result failure(final Object instance, final Exception exception) {
        return new Result(instance, exception);
    }

    ///////////////////////////////////////////////
    ////////////////// constructors ///////////////
    ///////////////////////////////////////////////
    /** Creates successful result. */
    public Result(final Object instance, final String message) {
        this.instance = instance;
        this.message = message;
        this.ex = null;
    }

    /** Creates successful result. */
    public Result(final String msg) {
        this.instance = null;
        this.message = msg;
        this.ex = null;
    }

    /** Creates failed result. */
    public Result(final Object instance, final Exception ex) {
        super(ex);
        this.instance = instance;
        this.message = ex.getMessage();
        this.ex = ex;
    }

    /** Creates failed result. */
    public Result(final Exception ex) {
        super(ex);
        this.instance = null;
        this.message = ex.getMessage();
        this.ex = ex;
    }

    @Override
    public String getMessage() {
        // There are exceptions that have no message, returning null.
        // This is not very useful and in fact was confusing in practice.
        // Let's return a full name of the exception in such cases.
        //return message != null ? message : ex != null ? ex.getMessage() : "no message";
        return message != null ? message : ex != null ? !isEmpty(ex.getMessage()) ? ex.getMessage() : ex instanceof NullPointerException ? NULL_POINTER_EXCEPTION : ex.getClass().getName() : "no message";
    }

    public Exception getEx() {
        return ex;
    }

    public Object getInstance() {
        return instance;
    }

    public <T> T getInstance(final Class<T> expectedType) {
        return expectedType.cast(instance);
    }

    /**
     * Mapping over a successful result.
     * <p>
     * Function {@code f} is applied to {@code this} if it is successful, and the returned result is the result of {@code f(this)}.
     * Otherwise, {@code this} is returned.
     *
     * @param f
     * @return
     */
    public Result map(final Function<? super Result, ? extends Result> f) {
        requireNonNull(f);
        return this.isSuccessful() ? f.apply(this) : this;
    }

    /**
     * A convenient method to get an instance associated with a successful result or throw an exception otherwise.
     * This method is analogous to {@link Optional#orElseThrow(Supplier)}.
     *
     * @param expectedType
     * @return
     */
    public <T> T getInstanceOrElseThrow() {
        ifFailure(Result::throwRuntime);
        return (T) getInstance();
    }

    /**
     * Copies this result with overridden instance.
     *
     * @param anotherInstance
     * @return
     */
    public Result copyWith(final Object anotherInstance) {
        return new Result(anotherInstance, message, ex);
    }

    public boolean isSuccessful() {
        return ex == null;
    }

    /**
     * A convenient construct to perform some action for a result that represents a failure.
     * For example, it could be used to throw an exception as it often happens in case of unsuccessful validations.
     *
     * @param consumer
     */
    public void ifFailure(final Consumer<? super Exception> consumer) {
        if (!isSuccessful()) {
            consumer.accept(ex);
        }
    }

    /**
     * A convenient method that returns the passed in <code>ex</code> if it is of type {@link Result}, or wraps it into a <code>failure</code> of type {@link Result}.
     *
     * @param ex
     * @return
     */
    public static RuntimeException asRuntime(final Exception ex) {
        return ex instanceof RuntimeException ? (RuntimeException) ex : failure(ex);
    }

    /**
     * A convenient method to throw a runtime exception that is obtained by passing <code>ex</code> into {@link asRuntime}.
     *
     * @param ex
     */
    public static void throwRuntime(final Exception ex) {
        throw asRuntime(ex);
    }

    /**
     * Returns true if this {@link Result} is not {@link Warning} instance and is successful.
     *
     * @return
     */
    public boolean isSuccessfulWithoutWarning() {
        return isSuccessful() && !(this instanceof Warning);
    }

    /**
     * Returns true if this {@link Result} is not {@link Warning} / {@link Informative} instance and is successful.
     *
     * @return
     */
    public boolean isSuccessfulWithoutWarningAndInformative() {
        return isSuccessfulWithoutWarning() && !(this instanceof Informative);
    }

    /**
     * Returns true only if this {@link Result} is successful and is instance of {@link Warning} class.
     *
     * @return
     */
    public boolean isWarning() {
        return isSuccessful() && this instanceof Warning;
    }

    /**
     * Returns true only if this {@link Result} is successful and is instance of {@link Informative} class.
     *
     * @return
     */
    public boolean isInformative() {
        return isSuccessful() && this instanceof Informative;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public int hashCode() {
        if (ex == null && message == null && instance == null) {
            return 0;
        }

        int result = 1;
        result = 31 * result + (ex == null ? 0 : (ex.getClass().hashCode() + (ex.getMessage() != null ? ex.getMessage().hashCode() : 0)));
        result = 31 * result + (message == null ? 0 : message.hashCode());
        result = 31 * result + (instance == null ? 0 : instance.hashCode());
        return result;
    }

    /**
     * There are three significant fields -- {@code ex}, {@code message} and {@code instance} -- that determine uniqueness of {@code Result} instance.
     * Exceptions may not have {@code equals} overridden for them.
     * This is why, two values of field {@code ex} are considered equal if their types and messages are identical.
     * Equality of fields {@code message} and {@code instance} rely upon their respective implementations of {@code equals}.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Result)) {
            return false;
        }

        final Result that = (Result) obj;
        return (this.ex == null && that.ex == null ||
                (this.ex != null && that.ex != null && Objects.equals(this.ex.getClass(), that.ex.getClass()) && Objects.equals(this.ex.getMessage(), that.ex.getMessage()))) &&
               Objects.equals(this.message, that.message) &&
               Objects.equals(this.instance, that.instance);
    }

    /**
     * Returns a pair of { shortMessage: ..., extendedMessage: ... } messages, associated with the {@code result}.
     * The {@code result} may be of type {@link Result}, {@link Warning} or {@link Informative}.
     * 
     * {@code result} message should never be {@code null} (see {@link Result#getMessage()}), except the case where NPE was causing failure {@link Result} -- in this case we return 'Null pointer exception' for both short and extended messages.
     * If {@link Result} was constructed with single message, it will be used for both short and extended messages.
     * Otherwise we do splitting by <extended/> part (and try to be smart if there are missing parts before or after <extended/>).
     */
    public static ResultMessages resultMessages(final Result result) {
        final String message = result.getMessage();
        if (message == null) {
            return new ResultMessages(NULL_POINTER_EXCEPTION, NULL_POINTER_EXCEPTION);
        } else if (!isEmpty(message)) {
            final String[] messages = message.split(EXT_SEPARATOR_PATTERN);
            final String firstMatch = messages.length > 0 ? messages[0] : "";
            final String secondMatch = messages.length > 1 ? messages[1] : "";
            final String shortMessage = !isEmpty(firstMatch) ? firstMatch : secondMatch;
            final String extendedMessage = !isEmpty(secondMatch) ? secondMatch : shortMessage;
            return new ResultMessages(shortMessage, extendedMessage);
        } else {
            return new ResultMessages(message, message);
        }
    }

    /**
     * A pair of { shortMessage: ..., extendedMessage: ... } messages, associated with the {@link Result}.
     * 
     * @author TG Team
     */
    public static class ResultMessages {
        public final String shortMessage;
        public final String extendedMessage;

        private ResultMessages(final String shortMessage, final String extendedMessage) {
            this.shortMessage = shortMessage;
            this.extendedMessage = extendedMessage;
        }
    }

    /**
     * Creates a copy of {@code result} with new instance representing properly serialisable {@link ArrayList} of previous instance and {@code customObject} map.
     * 
     * @param customObject
     * @return
     */
    public Result extendResultWithCustomObject(final Map<String, Object> customObject) {
        return copyWith(listOf(getInstance(), customObject));
    }

}