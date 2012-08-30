package ua.com.fielden.platform.expression.lexer.function.minute;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after the word 'MINUTE'.
 *
 * @author TG Team
 *
 */
public class State6 extends AbstractState {

    public State6() {
	super("S6", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	} else if (symbol == '(') {
	    return getAutomata().getState("S7");
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
