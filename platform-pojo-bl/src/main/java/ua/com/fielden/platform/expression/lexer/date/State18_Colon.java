package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State18_Colon extends AbstractState {

    public State18_Colon() {
	super("S18", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == ':') {
	    return getAutomata().getState("S19");
	}
	throw new NoTransitionAvailable("Expecting a colon (:) separator between hours and minutes.", this, symbol);
    }

}
