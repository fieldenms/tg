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
