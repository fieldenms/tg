package ua.com.fielden.platform.expression.lexer.function.max;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final and white space handling state.
 * 
 * @author TG Team
 * 
 */
public class State2 extends AbstractState {

    public State2() {
        super("S2", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'x' || symbol == 'X') {
            return getAutomata().getState("S3");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
