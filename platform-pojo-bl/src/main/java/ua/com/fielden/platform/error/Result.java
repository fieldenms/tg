package ua.com.fielden.platform.error;

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

    /**
     * Convenient factory method for creating a successful result.
     * 
     * @param instance
     * @return
     */
    public static Result successful(final Object instance) {
        return new Result(instance, "Successful");
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

    public Result(final Object instance, final String message, final Exception ex) {
        this.instance = instance;
        this.message = message;
        this.ex = ex;
    }

    public Result(final Object instance, final String message) {
        this(instance, message, null);
    }

    public Result(final Object instance, final Exception ex) {
        this(instance, null, ex);
    }

    /**
     * Creates unsuccessful result with provided exception.
     * 
     * @param ex
     */
    public Result(final Exception ex) {
        this(null, null, ex);
    }

    /**
     * Creates successful result with provided message.
     * 
     * @param msg
     */
    public Result(final String msg) {
        this(null, msg, null);
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

    public boolean isSuccessful() {
        return ex == null;
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
