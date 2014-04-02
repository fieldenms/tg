package ua.com.fielden.platform.expression.lexer.function.months;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 * 
 * @author TG Team
 * 
 */
public class State7 extends AbstractState {

    public State7() {
        super("S7", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
