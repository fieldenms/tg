package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State04_Year extends AbstractState {

    public State04_Year() {
        super("S4", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '9') {
            return getAutomata().getState("S5");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the fourth position in a four digit year representation.", this, symbol);
    }

}
