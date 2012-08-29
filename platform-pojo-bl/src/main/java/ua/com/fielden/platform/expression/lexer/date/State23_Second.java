package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State23_Second extends AbstractState {

    public State23_Second() {
	super("S23", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '9') {
	    return getAutomata().getState("S24");
	}
	throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the second position in a two digit second representation.", this, symbol);
    }

}
