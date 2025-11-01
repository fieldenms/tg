package ua.com.fielden.platform.expression.lexer.bool.true_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/// Non-terminal and non-final state.
///
public class State5DeadEnd extends AbstractState {

    public State5DeadEnd() {
        super("S5", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
