package ua.com.fielden.platform.expression.lexer.function.seconds;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State6 extends AbstractState {

    public State6() {
	super("S6", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == 's' || symbol == 'S') {
	    return getAutomata().getState("S7");
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
