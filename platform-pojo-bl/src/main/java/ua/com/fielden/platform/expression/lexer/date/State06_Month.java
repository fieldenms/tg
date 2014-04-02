package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State06_Month extends AbstractState {

    public State06_Month() {
        super("S6", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == '0') {
            return getAutomata().getState("S7");
        } else if (symbol == '1') {
            return getAutomata().getState("S8");
        }
        throw new NoTransitionAvailable("Expecting a digit 0 or 1 for the fourth position in a two digit month representation.", this, symbol);
    }

}
