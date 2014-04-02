package ua.com.fielden.platform.expression.lexer.string;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.NonDeterministicAutomata;

public class State1 extends AbstractState {

    public State1() {
        super("S1", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == '"') {
            return getAutomata().getState("S2");
        } else if (symbol == NonDeterministicAutomata.EOF) {
            throw new NoTransitionAvailable("Missing closing '\"'", this, symbol);
        } else if (symbol != '\\') {
            return this;
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
