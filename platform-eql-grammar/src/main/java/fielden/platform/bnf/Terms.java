package fielden.platform.bnf;

/**
 * Utilities that operate on {@linkplain Term terms}.
 */
public final class Terms {

    public static Terminal label(final String label, final Terminal terminal) {
        return terminal.annotate(TermMetadata.LABEL, label);
    }

    public static Variable label(final String label, final Variable variable) {
        return variable.annotate(TermMetadata.LABEL, label);
    }

    public static Terminal listLabel(String label, Terminal terminal) {
        return terminal.annotate(TermMetadata.LIST_LABEL, label);
    }

    public static Variable listLabel(String label, Variable variable) {
        return variable.annotate(TermMetadata.LIST_LABEL, label);
    }

    private Terms() {}

}
