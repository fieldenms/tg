package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State20_Minute extends AbstractState {

    public State20_Minute() {
        super("S20", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return getAutomata().getState("S21");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the second position in a two digit minute representation.", this, symbol);
    }

}
