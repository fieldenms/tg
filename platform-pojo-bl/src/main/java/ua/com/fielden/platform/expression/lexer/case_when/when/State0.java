package ua.com.fielden.platform.expression.lexer.case_when.when;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State0 extends AbstractState {

    public State0() {
	super("S0", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	} else if (symbol == 'w' || symbol == 'W') {
	    return getAutomata().getState("S1");
	}
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'" , this, symbol);
    }

}
