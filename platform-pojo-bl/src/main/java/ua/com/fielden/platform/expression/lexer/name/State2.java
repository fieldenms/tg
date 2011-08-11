package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * White space handling state.
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
	} else if (symbol == '.') {
	    return getAutomata().getState("S0");
	}
	throw new NoTransitionAvailable("Property should not contain white spaces.", this, symbol);
    }

}
