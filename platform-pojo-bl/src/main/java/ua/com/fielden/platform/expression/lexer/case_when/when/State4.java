package ua.com.fielden.platform.expression.lexer.case_when.when;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after keyword 'WHEN'. At least one WS or LPAREN is expected.
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
	    return getAutomata().getState("S5"); // final
	} else if (symbol == '(') {
	    return getAutomata().getState("S6"); // also final
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
