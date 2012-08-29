package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State10_Day extends AbstractState {

    public State10_Day() {
	super("S10", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == '0') {
	    return getAutomata().getState("S11");
	} else if (symbol >= '1' && symbol <= '2') {
	    return getAutomata().getState("S12");
	} else if (symbol == '3') {
	    return getAutomata().getState("S13");
	}
	throw new NoTransitionAvailable("Expecting a digit from range 0..3 for the first position in a two digit day representation.", this, symbol);
    }

}
