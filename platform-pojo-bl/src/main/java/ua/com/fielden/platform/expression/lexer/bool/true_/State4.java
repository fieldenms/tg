package ua.com.fielden.platform.expression.lexer.bool.true_;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/// Final terminal state.
///
public class State4 extends AbstractState {

    public State4() {
        super("S4", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol) || String.valueOf(symbol).matches("[^\\p{L}^\\d^.]") || symbol == ExpressionLexer.EOF) {
            throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
        } else {
            // This would happen only if string "true" would continue with some characters that is valid for name token.
            // For example, "truella", making it a valid name rather than keyword "true".
            // Thus, move to non-final state representing a dead-end for this automata.
            return getAutomata().getState("S5");
        }
    }

}