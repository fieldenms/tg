package ua.com.fielden.platform.expression.lexer.function.hour;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State2 extends AbstractState {

    public State2() {
	super("S2", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == 'u' || symbol == 'U') {
	    return getAutomata().getState("S3");
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
