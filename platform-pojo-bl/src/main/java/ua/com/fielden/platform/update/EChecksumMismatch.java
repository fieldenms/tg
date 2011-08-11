package ua.com.fielden.platform.update;

/**
 * A runtime exception indicating a mismatch between expected and the actual checksums.
 * 
 * @author TG Team
 * 
 */
public class EChecksumMismatch extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EChecksumMismatch(final String expectedChecksum, final String actualChecksum) {
	super("Mismatch between the expected " + expectedChecksum + " and the actual " + actualChecksum + " checksums.");
    }
}
