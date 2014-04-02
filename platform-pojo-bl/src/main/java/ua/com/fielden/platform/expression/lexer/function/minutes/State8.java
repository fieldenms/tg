package ua.com.fielden.platform.expression.lexer.function.minutes;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 * 
 * @author TG Team
 * 
 */
public class State8 extends AbstractState {

    public State8() {
        super("S8", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
