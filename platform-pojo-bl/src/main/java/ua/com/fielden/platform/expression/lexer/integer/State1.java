package ua.com.fielden.platform.expression.lexer.integer;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State1 extends AbstractState {

    public State1() {
        super("S1", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return this;
        } else if (isWhiteSpace(symbol)) {
            return getAutomata().getState("S2");
        }
        throw new NoTransitionAvailable("Integer number should not contain '" + symbol + "'", this, symbol);
    }

}
