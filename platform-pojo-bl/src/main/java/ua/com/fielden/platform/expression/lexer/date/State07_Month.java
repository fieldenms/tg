package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State07_Month extends AbstractState {

    public State07_Month() {
	super("S7", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '1' && symbol <= '9') {
	    return getAutomata().getState("S9");
	}
	throw new NoTransitionAvailable("Expecting a digit from range 1..9 for the second position in a two digit month representation.", this, symbol);
    }

}
