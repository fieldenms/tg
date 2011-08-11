package ua.com.fielden.platform.expression.lexer.minus;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State1 extends AbstractState {

    public State1() {
	super("S1", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'",this, symbol);
    }

}
