package ua.com.fielden.platform.expression.lexer.function.hours;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State0 extends AbstractState {

    public State0() {
        super("S0", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'h' || symbol == 'H') {
            return getAutomata().getState("S1");
        } else if (isWhiteSpace(symbol)) {
            return this;
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
