package ua.com.fielden.platform.expression.lexer.function.month;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after the word 'MONTH'.
 * 
 * @author TG Team
 * 
 */
public class State5 extends AbstractState {

    public State5() {
        super("S5", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == '(') {
            return getAutomata().getState("S6");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
