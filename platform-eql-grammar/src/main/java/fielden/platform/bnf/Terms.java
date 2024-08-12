package fielden.platform.bnf;

import static fielden.platform.bnf.Sequence.seqOrTerm;

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

    public static Term altLabel(final String label, final Term term, final Term... terms) {
        return seqOrTerm(term, terms).annotate(Metadata.altLabel(label));
    }

    public static Variable altLabel(final String label, final Variable variable) {
        return variable.annotate(Metadata.altLabel(label));
    }

    private Terms() {}

}
