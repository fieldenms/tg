package ua.com.fielden.platform.expression.lexer.function.day_diff;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 *
 * @author TG Team
 *
 */
public class State9 extends AbstractState {

    public State9() {
	super("S9", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
