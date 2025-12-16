package ua.com.fielden.platform.error;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

/// Represents a result of a computation, which could describe success, failure, warning or contain an informative message.
/// Such a computation could represent validation or application of some other business rules.
///
/// A result can be empty, or it can contain a value, which can be accessed via [#getInstance].
///
/// The kind of a result can be determined using methods [#isSuccessful], [#isWarning], [#isInformative].
///
/// `Result` is an exception type, and thus can also be thrown.
///
public class Result extends RuntimeException {

    // Field names
    public static final String MESSAGE = "message", INSTANCE = "instance", EX = "ex";

    private static final String SUCCESSFUL = "Successful";
    private static final String NULL_POINTER_EXCEPTION = "Null pointer exception";
    private static final String EXT_SEPARATOR = "<extended/>";
    private static final String EXT_SEPARATOR_PATTERN = quote(EXT_SEPARATOR);

    private final Exception ex;
    private final String message;
    private final Object instance;

    /// Mainly used for serialisation.
    ///
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

    /// Returns an empty successful result.
    ///
    public static Result successful() {
        return successful(null);
    }

    /// Creates a successful [Result] that will contain `instance`.
    ///
    public static Result successful(final Object instance) {
        return new Result(instance, SUCCESSFUL);
    }

    ///////////////////////////////////////////// Informative /////////////////////////////////////////////

    public static Informative informative(final String message) {
        return informative(null, message);
    }

    /// Creates an informative result whose message is constructed by formatting `fmt` with `args`.
    ///
    public static Informative informativef(final String fmt, final Object... args) {
        return informative(format(fmt, args));
    }

    /// Creates an informative result with an additional extended messsage.
    ///
    /// @param shortMessage a concise message, which may contain HTML.
    /// @param extendedMessage a more detailed message, which may span multiple lines and contain HTML.
    ///
    public static Informative informativeEx(final String shortMessage, final String extendedMessage) {
        return informative(shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    /// Creates an informative result that will contain `instance` and have message `message`.
    ///
    public static Informative informative(final Object instance, final String message) {
        return new Informative(instance, message);
    }

    /// Creates an informative result with an additional extended messsage.
    ///
    /// @param instance a value that the result will contain.
    /// @param shortMessage a concise message, which may contain HTML.
    /// @param extendedMessage a more detailed message, which may span multiple lines and contain HTML.
    ///
    public static Informative informativeEx(final Object instance, final String shortMessage, final String extendedMessage) {
        return informative(instance, shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    ///////////////////////////////////////////// Warning /////////////////////////////////////////////

    public static Warning warning(final String message) {
        return warning(null, message);
    }

    /// Creates a warning whose message is constructed by formatting `fmt` with `args`.
    ///
    public static Warning warningf(final String fmt, final Object... args) {
        return warning(format(fmt, args));
    }

    /// Creates a warning with an additional extended messsage.
    ///
    /// @param shortMessage a concise message, which may contain HTML.
    /// @param extendedMessage a more detailed message, which may span multiple lines and contain HTML.
    ///
    public static Warning warningEx(final String shortMessage, final String extendedMessage) {
        return warning(shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    /// Creates a warning that will contain `instance` and have message `message`.
    ///
    public static Warning warning(final Object instance, final String message) {
        return new Warning(instance, message);
    }

    /// Creates a warning with an additional extended messsage.
    ///
    /// @param instance a value that the result will contain.
    /// @param shortMessage a concise message, which may contain HTML.
    /// @param extendedMessage a more detailed message, which may span multiple lines and contain HTML.
    ///
    public static Warning warningEx(final Object instance, final String shortMessage, final String extendedMessage) {
        return warning(instance, shortMessage + EXT_SEPARATOR + extendedMessage);
    }

    ///////////////////////////////////////////// Failure /////////////////////////////////////////////

    /// Creates a failure with message `reason`.
    /// Should be used when neither an object in error nor the cause are important.
    ///
    public static Result failure(final String reason) {
        return failure(null, reason);
    }

    /// Creates a failure whose message is constructed by formatting `fmt` with `args`.
    /// Should be used when neither an object in error nor the cause are important.
    ///
    public static Result failuref(final String fmt, final Object... args) {
        return failure(format(fmt, args));
    }

    /// Creates a warning with an additional extended messsage.
    /// Should be used when neither an object in error nor the cause are important.
    ///
    /// @param shortReason a concise message, which may contain HTML.
    /// @param extendedReason a more detailed message, which may span multiple lines and contain HTML.
    ///
    public static Result failureEx(final String shortReason, final String extendedReason) {
        return failure(shortReason + EXT_SEPARATOR + extendedReason);
    }

    /// Creates a failure from its cause `exception`.
    ///
    public static Result failure(final Exception exception) {
        return failure(null, exception);
    }

    /// Creates a failure that will contain `instance` and have message `message`.
    /// Should be used when the cause is not important.
    ///
    public static Result failure(final Object instance, final String reason) {
        return failure(instance, new Exception(reason));
    }

    /// Creates a failure with an additional extended messsage.
    /// Should be used when no particular exception is at fault.
    ///
    /// @param instance a value that the result will contain.
    /// @param shortReason a concise message, which may contain HTML.
    /// @param extendedReason a more detailed message, which may span multiple lines and contain HTML.
    ///
    public static Result failureEx(final Object instance, final String shortReason, final String extendedReason) {
        return failure(instance, shortReason + EXT_SEPARATOR + extendedReason);
    }

    /// Creates a failure that will contain `instance` and have `exception` as its cause.
    ///
    public static Result failure(final Object instance, final Exception exception) {
        return new Result(instance, exception);
    }

    /// @deprecated Use [#successful(Object)].
    ///
    @Deprecated(forRemoval = true)
    public Result(final Object instance, final String message) {
        this.instance = instance;
        this.message = message;
        this.ex = null;
    }

    /// @deprecated Use [#successful()].
    ///
    @Deprecated(forRemoval = true)
    public Result(final String msg) {
        this.instance = null;
        this.message = msg;
        this.ex = null;
    }

    /// @deprecated Use [#failure(Object, Exception)].
    ///
    @Deprecated(forRemoval = true)
    public Result(final Object instance, final Exception ex) {
        super(ex);
        this.instance = instance;
        this.message = ex.getMessage();
        this.ex = ex;
    }

    /// @deprecated Use [#failure(Exception)].
    ///
    @Deprecated(forRemoval = true)
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

    /// Mapping over a successful result.
    ///
    /// If `this` is successful, returns `f(this)`.
    /// Otherwise, returns `this`.
    ///
    public Result map(final Function<? super Result, ? extends Result> f) {
        requireNonNull(f);
        return this.isSuccessful() ? f.apply(this) : this;
    }

    /// If this result is not a failure, returns the contained value.
    /// Otherwise, throws this result.
    ///
    /// This method is analogous to [Optional#orElseThrow(Supplier)].
    ///
    public <T> T getInstanceOrElseThrow() {
        ifFailure(Result::throwRuntime);
        return (T) getInstance();
    }

    /// Returns a copy of this result, replacing the contained value by `anotherInstance`.
    ///
    public Result copyWith(final Object anotherInstance) {
        return new Result(anotherInstance, message, ex);
    }

    public boolean isSuccessful() {
        return ex == null;
    }

    /// If this result is a failure, calls `consumer` with the cause of the failure.
    ///
    /// This method is used in a common pattern:
    ///
    /// ```
    /// myResult.ifFailure(Result::throwRuntime);
    /// ```
    ///
    /// @return this result
    ///
    public Result ifFailure(final Consumer<? super Exception> consumer) {
        if (!isSuccessful()) {
            consumer.accept(ex);
        }
        return this;
    }

    /// If `ex` is a runtime exception, returns it, otherwise returns a failure [Result] with `ex` as its cause.
    ///
    public static RuntimeException asRuntime(final Exception ex) {
        return ex instanceof RuntimeException ? (RuntimeException) ex : failure(ex);
    }

    /// Throws `ex` as a runtime exception.
    ///
    public static void throwRuntime(final Exception ex) {
        throw asRuntime(ex);
    }

    /// Returns `true` if this result is successful and is not a [warning][Warning].
    ///
    public boolean isSuccessfulWithoutWarning() {
        return isSuccessful() && !(this instanceof Warning);
    }

    /// Returns `true` if this result is successful and is not a [warning][Warning], nor an [informative][Informative].
    ///
    public boolean isSuccessfulWithoutWarningAndInformative() {
        return isSuccessfulWithoutWarning() && !(this instanceof Informative);
    }

    /// Returns `true` if this result is a [warning][Warning].
    ///
    public boolean isWarning() {
        return isSuccessful() && this instanceof Warning;
    }

    /// Returns `true` if this result is an [informative][Informative].
    ///
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

    /// There are three significant fields -- `ex`, `message` and `instance` -- that determine uniqueness of a `Result` instance.
    /// Exceptions may not have `equals` overridden.
    /// This is why, two values of field `ex` are considered equal if their types and messages are identical.
    /// Equality of fields `message` and `instance` rely upon their respective implementations of `equals`.
    ///
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

    /// Returns a pair of messages, short and extended, associated with `result`.
    /// If `result` has a single message, it will be used for both short and extended messages.
    ///
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

    /// A pair of messages, short and extended, associated with a [Result].
    ///
    public static class ResultMessages {
        public final String shortMessage;
        public final String extendedMessage;

        private ResultMessages(final String shortMessage, final String extendedMessage) {
            this.shortMessage = shortMessage;
            this.extendedMessage = extendedMessage;
        }
    }

    /// Creates a copy of this result that will contain a serialisable list of the value contained by this result and `customObject`.
    ///
    public Result extendResultWithCustomObject(final Map<String, Object> customObject) {
        return copyWith(listOf(getInstance(), customObject));
    }

}
