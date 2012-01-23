package ua.com.fielden.platform.expression.lexer.function.max;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 *
 * @author TG Team
 *
 */
public class State4 extends AbstractState {

    public State4() {
	super("S4", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
