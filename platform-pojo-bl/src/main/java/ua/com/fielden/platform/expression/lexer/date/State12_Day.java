package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State12_Day extends AbstractState {

    public State12_Day() {
        super("S12", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return getAutomata().getState("S14");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the second position in a two digit day representation.", this, symbol);
    }

}
