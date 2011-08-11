package auction.command;

/**
 * A checked exception thrown by command execute methods, wrapping the root cause.
 *
 * @author Christian Bauer
 */
//@ApplicationException(rollback = true)
public class CommandException extends Exception {

    public CommandException() {
    }

    public CommandException(final String message) {
	super(message);
    }

    public CommandException(final String message, final Throwable cause) {
	super(message, cause);
    }

    public CommandException(final Throwable cause) {
	super(cause);
    }
}
