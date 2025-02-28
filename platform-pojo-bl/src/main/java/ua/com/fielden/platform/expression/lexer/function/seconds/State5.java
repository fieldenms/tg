package ua.com.fielden.platform.expression.lexer.function.seconds;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State5 extends AbstractState {

    public State5() {
        super("S5", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'd' || symbol == 'D') {
            return getAutomata().getState("S6");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
