package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State00_Quote extends AbstractState {

    public State00_Quote() {
        super("S0", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == '\'') {
            return getAutomata().getState("S1");
        } else if (isWhiteSpace(symbol)) {
            return this;
        }
        throw new NoTransitionAvailable("Expecting a single quote (') that indicates the start of a date literal.", this, symbol);
    }

}
