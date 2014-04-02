package ua.com.fielden.platform.expression.lexer.function.minutes;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Handles white space after the word 'MINUTES' and expects LPAREN.
 * 
 * @author TG Team
 * 
 */
public class State7 extends AbstractState {

    public State7() {
        super("S7", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == '(') {
            return getAutomata().getState("S8");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
