package ua.com.fielden.platform.expression.lexer.function.avg;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after the word 'AVG'.
 *
 * @author TG Team
 *
 */
public class State3 extends AbstractState {

    public State3() {
	super("S3", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	} else if (symbol == '(') {
	    return getAutomata().getState("S4");
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
