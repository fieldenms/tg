package ua.com.fielden.platform.expression.automata;

public class NoTransitionAvailable extends Exception {
    public final AbstractState fromState;
    public final char symbol;

    public NoTransitionAvailable(final String message, final AbstractState fromState, final char symbol) {
	super(message);
	this.fromState = fromState;
	this.symbol = symbol;
    }
}
