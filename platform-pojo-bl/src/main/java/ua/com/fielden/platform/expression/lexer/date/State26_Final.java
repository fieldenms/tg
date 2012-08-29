package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state for both the date and time portions of the date literal.
 *
 * @author TG Team
 *
 */
public class State26_Final extends AbstractState {

    public State26_Final() {
	super("FINAL", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) { // give it a chance with accidental white spaces after the literal
	    return this;
	} else  {
	    throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
	}
    }

}
