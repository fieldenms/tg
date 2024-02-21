package fielden.platform.bnf;

public sealed abstract class LabeledTerm<T extends Term> implements Term {

    public final String label;
    public final T term;

    protected LabeledTerm(String label, T term) {
        this.label = label;
        this.term = term;
    }

    public static LabeledVariable label(String label, Variable variable) {
        return new LabeledVariable(label, variable);
    }

    public static LabeledTerminal label(String label, Terminal terminal) {
        return new LabeledTerminal(label, terminal);
    }

    public static final class LabeledVariable extends LabeledTerm<Variable> implements Variable {
        public LabeledVariable(final String label, final Variable term) {
            super(label, term);
        }

        @Override
        public String name() {
            return term.name();
        }
    }

    public static final class LabeledTerminal extends LabeledTerm<Terminal> implements Terminal {
        public LabeledTerminal(final String label, final Terminal term) {
            super(label, term);
        }

        @Override
        public String name() {
            return term.name();
        }
    }

}
