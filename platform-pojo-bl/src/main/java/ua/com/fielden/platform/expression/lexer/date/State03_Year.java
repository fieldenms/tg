package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State03_Year extends AbstractState {

    public State03_Year() {
        super("S3", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return getAutomata().getState("S4");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the third position in a four digit year representation.", this, symbol);
    }

}
