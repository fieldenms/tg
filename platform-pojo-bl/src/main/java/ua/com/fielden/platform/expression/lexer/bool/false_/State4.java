package ua.com.fielden.platform.expression.lexer.bool.false_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State4 extends AbstractState {

    public State4() {
        super("S4", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'e' || symbol == 'E') {
            return getAutomata().getState("S5");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}