package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State1 extends AbstractState {

    public State1() {
	super("S1", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if ((symbol >= 'a' && symbol <= 'z') || (symbol >= 'A' && symbol <= 'Z') || (symbol >= '0' && symbol <= '9') || symbol == '_') {
	    return this;
	} else if (symbol == '.') {
	    return getAutomata().getState("S0");
	} else if (isWhiteSpace(symbol)) {
	    return getAutomata().getState("S2");
	}
	throw new NoTransitionAvailable("Property name should not contain '" + symbol + "'", this, symbol);
    }

}
