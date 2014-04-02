package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State01_Year extends AbstractState {

    public State01_Year() {
        super("S1", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '1' && symbol <= '9') {
            return getAutomata().getState("S2");
        } else if (isWhiteSpace(symbol)) {
            return this;
        }
        throw new NoTransitionAvailable("Expecting a digit from range 1..9 for the first position in a four digit year representation.", this, symbol);
    }

}
