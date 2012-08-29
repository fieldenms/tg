package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State21_Colon extends AbstractState {

    public State21_Colon() {
	super("S21", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == ':') {
	    return getAutomata().getState("S22");
	}
	throw new NoTransitionAvailable("Expecting a colon (:) separator between minutes and seconds.", this, symbol);
    }

}
