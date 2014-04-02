package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State0 extends AbstractState {

    public State0() {
        super("S0", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (isWhiteSpace(symbol)) {
            return this;
        } else if (symbol == '←') {
            return getAutomata().getState("S1");
        } else if ((symbol >= 'a' && symbol <= 'z') || (symbol >= 'A' && symbol <= 'Z') || symbol == '_') {
            return getAutomata().getState("S2");
        }
        throw new NoTransitionAvailable("Property name should not start with '" + symbol + "'", this, symbol);
    }

}
