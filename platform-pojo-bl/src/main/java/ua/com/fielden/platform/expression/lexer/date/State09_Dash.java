package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State09_Dash extends AbstractState {

    public State09_Dash() {
	super("S9", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == '-') {
	    return getAutomata().getState("S10");
	}
	throw new NoTransitionAvailable("Expecting a dash (-) separating month and day portions.", this, symbol);
    }

}
