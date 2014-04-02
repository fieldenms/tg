package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State08_Month extends AbstractState {

    public State08_Month() {
        super("S8", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '2') {
            return getAutomata().getState("S9");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..2 for the second position in a two digit month representation.", this, symbol);
    }

}
