package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State16_Hour extends AbstractState {

    public State16_Hour() {
	super("S16", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '9') {
	    return getAutomata().getState("S18");
	}
	throw new NoTransitionAvailable("Expecting a digit from range 0..9 for the second position in a two digit hour representation.", this, symbol);
    }

}
