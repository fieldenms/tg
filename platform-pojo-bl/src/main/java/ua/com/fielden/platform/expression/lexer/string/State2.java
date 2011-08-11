package ua.com.fielden.platform.expression.lexer.string;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final and white space handling state.
 *
 * @author TG Team
 *
 */
public class State2 extends AbstractState {

    public State2() {
	super("S2", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	}
	throw new NoTransitionAvailable("Only white spaces may follow a string value.", this, symbol);
    }

}
