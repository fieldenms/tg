package ua.com.fielden.platform.expression.lexer.decimal;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State3 extends AbstractState {

    public State3() {
	super("S3", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '9') {
	    return getAutomata().getState("S2");
	}
	throw new NoTransitionAvailable("Unexpected ending of the decimal number '" + symbol + "'", this, symbol);
    }

}
