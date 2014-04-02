package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State02_Year extends AbstractState {

    public State02_Year() {
        super("S2", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return getAutomata().getState("S3");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the second position in a four digit year representation.", this, symbol);
    }

}
