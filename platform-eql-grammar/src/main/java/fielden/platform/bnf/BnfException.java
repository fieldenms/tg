package fielden.platform.bnf;

public class BnfException extends RuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public BnfException(final String s) {
        super(s);
    }

    public BnfException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
