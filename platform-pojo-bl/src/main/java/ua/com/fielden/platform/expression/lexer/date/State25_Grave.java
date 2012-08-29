package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Grave state.
 *
 * @author TG Team
 *
 */
public class State25_Grave extends AbstractState {

    public State25_Grave() {
	super("GRAVE", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
