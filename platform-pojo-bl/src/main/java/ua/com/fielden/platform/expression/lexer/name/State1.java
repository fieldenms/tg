package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Together with state S0 handles the parent path indication as part of the property name.
 * 
 * @author TG Team
 * 
 */
public class State1 extends AbstractState {

    public State1() {
        super("S1", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == '.') {
            return getAutomata().getState("S0");
        }

        throw new NoTransitionAvailable("Incomplete property definition. Parent identifier should only be followed by a whitespace or a dot -- '" + symbol
                + "' is not allowed here.", this, symbol);
    }

}
