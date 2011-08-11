package ua.com.fielden.platform.expression.lexer.decimal;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State2 extends AbstractState {

    public State2() {
	super("S2", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '9') {
	    return this;
	}
	throw new NoTransitionAvailable("Unexpected ending of the decimal number '" + symbol + "'", this, symbol);
    }

}
