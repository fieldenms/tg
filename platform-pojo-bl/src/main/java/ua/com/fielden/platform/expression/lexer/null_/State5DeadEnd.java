package ua.com.fielden.platform.expression.lexer.null_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Non-final dead end state.
 *
 * @author TG Team
 *
 */
public class State5DeadEnd extends AbstractState {

    public State5DeadEnd() {
	super("S5", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
