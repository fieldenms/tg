package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State17_Hour extends AbstractState {

    public State17_Hour() {
	super("S17", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '3') {
	    return getAutomata().getState("S18");
	}
	throw new NoTransitionAvailable("Expecting a digit from range 0..3 for the second position in a two digit hour representation.", this, symbol);
    }

}
