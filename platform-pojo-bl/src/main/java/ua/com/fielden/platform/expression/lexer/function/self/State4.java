package ua.com.fielden.platform.expression.lexer.function.self;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * State that handles white space after the word 'SELF' and the closing parenthesis.
 * 
 * @author TG Team
 * 
 */
public class State4 extends AbstractState {

    public State4() {
        super("S4", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == ')') {
            return getAutomata().getState("S5");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
