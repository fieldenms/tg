package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State13_Day extends AbstractState {

    public State13_Day() {
	super("S12", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '1') {
	    return getAutomata().getState("S14");
	}
	throw new NoTransitionAvailable("Expecting digit 0 or 1 for the second position in a two digit day representation.", this, symbol);
    }

}
