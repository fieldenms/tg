package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.internal.grammar.rules.*;

import java.util.Objects;

public abstract sealed class LabeledTempSymbol<T extends TempSymbol> implements TempSymbol {
    protected final String label;
    protected final T symbol;

    LabeledTempSymbol(String label, T symbol) {
        this.label = label;
        this.symbol = symbol;
    }

    @Override
    public String name() {
        return symbol.name();
    }

    public String label() {
        return label;
    }

    public T symbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (LabeledTempSymbol) obj;
        return Objects.equals(this.label, that.label) &&
                Objects.equals(this.symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, symbol);
    }

    @Override
    public String toString() {
        return "%s=%s".formatted(label, symbol);
    }

    // if the label support needs to be extended to other types (e.g., Token)
    // create a corresponding class and a static method in a manner similar to LabeledVariable

    public static LabeledVariable label(String label, Variable variable) {
        return new LabeledVariable(label, variable);
    }

    public static final class LabeledVariable extends LabeledTempSymbol<Variable> implements Variable {
        public LabeledVariable(final String label, final Variable symbol) {
            super(label, symbol);
        }

        @Override
        public Symbol normalize() {
            return this;
        }
    }

    public static LabeledTerminal label(String label, Terminal variable) {
        return new LabeledTerminal(label, variable);
    }

    public static final class LabeledTerminal extends LabeledTempSymbol<Terminal> implements Terminal {
        public LabeledTerminal(final String label, final Terminal symbol) {
            super(label, symbol);
        }

        @Override
        public Token normalize() {
            return new Token(this);
        }
    }

}
