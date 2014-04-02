package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State15_Hour extends AbstractState {

    public State15_Hour() {
        super("S15", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == '0' || symbol == '1') {
            return getAutomata().getState("S16");
        } else if (symbol == '2') {
            return getAutomata().getState("S17");
        }
        throw new NoTransitionAvailable("Expecting a digit from range 0..3 for the first position in a two digit day representation.", this, symbol);
    }

}
