package ua.com.fielden.platform.expression.lexer.bool.true_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State3 extends AbstractState {

    public State3() {
        super("S3", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'e' || symbol == 'E') {
            return getAutomata().getState("S4");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}