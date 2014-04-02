package ua.com.fielden.platform.expression.lexer.null_;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 * 
 * @author TG Team
 * 
 */
public class State4 extends AbstractState {

    public State4() {
        super("S4", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol) || String.valueOf(symbol).matches("[^\\p{L}^\\d^.]") || symbol == ExpressionLexer.EOF) {
            throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
        } else {
            // this would happen only if string NULL would continue with some characters that is valid for name token
            // e.g. NULLed, making it a valid name rather than keyword NULL
            // thus move to non-final state representing a dead-end for this automata
            return getAutomata().getState("S5");
        }
    }
}
