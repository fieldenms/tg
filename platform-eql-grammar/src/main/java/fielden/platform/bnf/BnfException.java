package fielden.platform.bnf;

public class BnfException extends RuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public BnfException(String s) {
        super(s);
    }

    public BnfException(String message, Throwable cause) {
        super(message, cause);
    }

}
