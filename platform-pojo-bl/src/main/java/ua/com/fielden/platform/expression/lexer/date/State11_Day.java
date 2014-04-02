package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State11_Day extends AbstractState {

    public State11_Day() {
        super("S11", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '1' && symbol <= '9') {
            return getAutomata().getState("S14");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 1..9 for the second position in a two digit day representation.", this, symbol);
    }

}
