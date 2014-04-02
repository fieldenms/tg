package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State2 extends AbstractState {

    public State2() {
        super("S2", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if ((symbol >= 'a' && symbol <= 'z') || (symbol >= 'A' && symbol <= 'Z') || (symbol >= '0' && symbol <= '9') || symbol == '_') {
            return this;
        } else if (symbol == '.') {
            return getAutomata().getState("S3");
        } else if (isWhiteSpace(symbol)) {
            return getAutomata().getState("S4");
        }
        throw new NoTransitionAvailable("Property name should not contain '" + symbol + "'", this, symbol);
    }

}
