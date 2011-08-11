package ua.com.fielden.platform.expression.lexer.date_constant;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State1 extends AbstractState {

    public State1() {
	super("S1", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol >= '0' && symbol <= '9') {
	    return this;
	} else if (symbol == 'd' || symbol == 'm' || symbol == 'y') {
	    return getAutomata().getState("S2");
	}
	throw new NoTransitionAvailable("Expecting a digit from range 0..9 or indication of the date portion (d, m, y)",this, symbol);
    }

}
