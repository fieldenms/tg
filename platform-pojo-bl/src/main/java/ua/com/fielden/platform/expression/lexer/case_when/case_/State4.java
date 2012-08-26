package ua.com.fielden.platform.expression.lexer.case_when.case_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after keyword 'CASE'. At least one WS is expected.
 *
 * @author TG Team
 *
 */
public class State4 extends AbstractState {

    public State4() {
	super("S4", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return getAutomata().getState("S5");
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
