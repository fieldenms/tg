package ua.com.fielden.platform.expression.lexer.case_when.case_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 *
 * @author TG Team
 *
 */
public class State5 extends AbstractState {

    public State5() {
	super("S5", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
