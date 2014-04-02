package ua.com.fielden.platform.expression.lexer.decimal;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State0 extends AbstractState {

    public State0() {
        super("S0", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return getAutomata().getState("S1");
        } else if (symbol == '.') {
            return getAutomata().getState("S3");
        } else if (isWhiteSpace(symbol)) {
            return this;
        }
        throw new NoTransitionAvailable("Unexpected start of the decimal number '" + symbol + "'", this, symbol);
    }

}
