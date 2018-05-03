package ua.com.fielden.platform.error;

import static java.lang.String.format;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Objects;

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

    /**
     * Convenient factory method for creating a successful result.
     *
     * @param instance
     * @return
     */
    public static Result successful(final Object instance) {
        return new Result(instance, "Successful");
    }

    public static Warning warning(final String msg) {
        return new Warning(msg);
    }

    public static Warning warning(final Object instance, final String msg) {
        return new Warning(instance, msg);
    }

    /**
     * Convenient factory method for creating a failure result.
     *
     * @param instance
     *            -- instance that is in error
     * @param ex
     *            -- associated exception that caused failure
     * @return
     */
    public static Result failure(final Object instance, final Exception ex) {
        return new Result(instance, ex);
    }

    /**
     * Convenient factory method for creating a failure result. Should be used when no particular exception is at fault.
     *
     * @param instance
     *            -- instance that is in error
     * @param reason
     *            -- reason for failure.
     * @return
     */
    public static Result failure(final Object instance, final String reason) {
        return new Result(instance, new Exception(reason));
    }

    /**
     * Convenient factory method for creating a failure result. In some cases there is no need to pass in an instance that is in error -- just an error itself.
     *
     * @param ex
     *            -- exception that caused the failure.
     * @return
     */
    public static Result failure(final Exception ex) {
        return new Result(null, ex);
    }

    /**
     * Convenient factory method for creating a failure result. Should be used when neither an object in error nor the actual exception type are important.
     *
     * @param reason
     *            -- should describe the failure.
     * @return
     */
    public static Result failure(final String reason) {
        return new Result(null, new Exception(reason));
    }

    /**
     * The same as {@link #failure(String)} with with the semantics of {@link String#format(String, Object...)} for interpolating of the {@code reason} string.
     *
     * @param reason
     * @param args
     * @return
     */
    public static Result failure(final String reason, final Object...args) {
        return new Result(null, new Exception(format(reason, args)));
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
        return message != null ? message : ex != null ? ex.getMessage() : "no message";
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
     * Returns true only if this {@link Result} is successful and is instance of {@link Warning} class.
     *
     * @return
     */
    public boolean isWarning() {
        return isSuccessful() && this instanceof Warning;
    }

    @Override
    public String toString() {
        return getMessage();
    }

}
