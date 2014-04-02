package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State19_Minute extends AbstractState {

    public State19_Minute() {
        super("S19", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol >= '0' && symbol <= '5') {
            return getAutomata().getState("S20");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..5 for the first position in a two digit minute representation.", this, symbol);
    }

}
