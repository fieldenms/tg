package ua.com.fielden.platform.expression.lexer.or;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State1 extends AbstractState {

    public State1() {
        super("S1", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == '|') {
            return getAutomata().getState("S2");
        }
        throw new NoTransitionAvailable("'|' is expected.", this, symbol);
    }

}
