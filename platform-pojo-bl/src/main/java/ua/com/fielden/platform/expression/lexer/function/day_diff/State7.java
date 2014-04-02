package ua.com.fielden.platform.expression.lexer.function.day_diff;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State7 extends AbstractState {

    public State7() {
        super("S7", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'f' || symbol == 'F') {
            return getAutomata().getState("S8");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
