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

    private boolean encounteredWhiteSpace = false;
    
    public State3() {
        super("S3", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            encounteredWhiteSpace = true;
            return this;
        } else if (encounteredWhiteSpace || symbol == ExpressionLexer.EOF || symbol == ')') {
            encounteredWhiteSpace = false;
            throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
        } else {
            encounteredWhiteSpace = false;
            // this would happen only if string END would continue with some other characters e.g. ENDed, making it a valid name
            // rather than keyword END, thus move to non-final state representing a dead-end for this automata
            return getAutomata().getState("S4");
        }
    }

}
