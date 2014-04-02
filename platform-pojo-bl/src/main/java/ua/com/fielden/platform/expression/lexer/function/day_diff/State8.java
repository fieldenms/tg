package ua.com.fielden.platform.expression.lexer.function.day_diff;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after the word 'DAY_DIFF'.
 * 
 * @author TG Team
 * 
 */
public class State8 extends AbstractState {

    public State8() {
        super("S8", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == '(') {
            return getAutomata().getState("S9");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
