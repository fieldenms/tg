package ua.com.fielden.platform.expression.lexer.case_when.end;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 * 
 * @author TG Team
 * 
 */
public class State3 extends AbstractState {

    public State3() {
        super("S3", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == ExpressionLexer.EOF) {
            throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
        } else {
            // this would happen only if string END would continua with some other characters e.g. ENDed, making it a valid name
            // rather than keyword END, thus move to non-final state representing a dead-end for this automata
            return getAutomata().getState("S4");
        }
    }

}
