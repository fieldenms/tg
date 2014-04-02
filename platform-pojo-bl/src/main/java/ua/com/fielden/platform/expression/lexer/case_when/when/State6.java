package ua.com.fielden.platform.expression.lexer.case_when.when;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 * 
 * @author TG Team
 * 
 */
public class State6 extends AbstractState {

    public State6() {
        super("S6", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
