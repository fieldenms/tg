package fielden.platform.bnf;

public final class Terms {

    public static Terminal label(String label, Terminal terminal) {
        return terminal.annotate(TermMetadata.LABEL, label);
    }

    public static Variable label(String label, Variable variable) {
        return variable.annotate(TermMetadata.LABEL, label);
    }

    private Terms() {}

}
