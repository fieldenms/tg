package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State05_Dash extends AbstractState {

    public State05_Dash() {
	super("S5", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == '-') {
	    return getAutomata().getState("S6");
	}
	throw new NoTransitionAvailable("Expecting a dash (-) separating year and month portions.", this, symbol);
    }

}
