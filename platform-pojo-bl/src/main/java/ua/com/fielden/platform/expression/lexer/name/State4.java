package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * White space handling state.
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
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == '.') {
            return getAutomata().getState("S3");
        }
        throw new NoTransitionAvailable("Property name should not contain white spaces.", this, symbol);
    }

}
