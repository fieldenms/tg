package ua.com.fielden.platform.expression.lexer.bool.false_;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/// Non-terminal and non-final state.
///
public class State6DeadEnd extends AbstractState {

    public State6DeadEnd() {
        super("S6", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
