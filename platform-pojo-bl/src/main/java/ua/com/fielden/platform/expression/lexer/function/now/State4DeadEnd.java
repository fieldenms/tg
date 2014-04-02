package ua.com.fielden.platform.expression.lexer.function.now;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Non-final dead end state.
 * 
 * @author TG Team
 * 
 */
public class State4DeadEnd extends AbstractState {

    public State4DeadEnd() {
        super("S4", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
