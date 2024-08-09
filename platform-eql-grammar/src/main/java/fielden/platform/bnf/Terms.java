package fielden.platform.bnf;

/**
 * Utilities that operate on {@linkplain Term terms}.
 */
public final class Terms {

    public static Terminal label(final String label, final Terminal terminal) {
        return terminal.annotate(Metadata.label(label));
    }

    public static Variable label(final String label, final Variable variable) {
        return variable.annotate(Metadata.label(label));
    }

    public static Terminal listLabel(String label, Terminal terminal) {
        return terminal.annotate(Metadata.listLabel(label));
    }

    public static Variable listLabel(String label, Variable variable) {
        return variable.annotate(Metadata.listLabel(label));
    }

    private Terms() {}

}
