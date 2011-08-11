package ua.com.fielden.platform.expression.automata;

/**
 * A base class for implementing a state used in non-deterministic automata.
 *
 * @author TG Team
 *
 */
public abstract class AbstractState {
    public final String name;
    public final boolean isFinal;
    private NonDeterministicAutomata automata;
    private char transitionSymbol;

    public AbstractState(final String name, final boolean isFinal) {
	this.name = name;
	this.isFinal = isFinal;
    }

    public final AbstractState accept(final char symbol) throws NoTransitionAvailable {
	if (automata == null) {
	    throw new IllegalStateException("Automata has to be associated with a state before it may accept any input.");
	} else if (automata.getFailure() != null) {
	    throw new IllegalStateException("Automata has terminated with recognition exception and cannot continue recognition without being reset.");
	}
	try {
	    final AbstractState newState = transition(symbol);
	    newState.transitionSymbol = symbol;
	    automata.setCurrSate(newState);
	    return newState;
	} catch (final NoTransitionAvailable ex) {
	    automata.setFailure(ex);
	    throw ex;
	}

    }

    /** WS : (' '|'\t'|'\n'|'\r')* ; // ignore any whitespace */
    protected final boolean isWhiteSpace(final char symbol) {
	return symbol == ' ' || symbol == '\t' || symbol == '\n' || symbol == '\r';
    }

    /**
     * Needs to be implemented to provide transition logic from this state for the specified input symbol.
     *
     * @param symbol
     *            -- input
     * @return
     * @throws NoTransitionAvailable
     */
    protected abstract AbstractState transition(char symbol) throws NoTransitionAvailable;

    public NonDeterministicAutomata getAutomata() {
	return automata;
    }

    void setAutomata(final NonDeterministicAutomata automata) {
	if (this.automata != null && !this.automata.equals(automata)) {
	    throw new IllegalArgumentException("Automata cannot be changed once associated with a state.");
	}
	this.automata = automata;
    }

    public char getTransitionSymbol() {
	return transitionSymbol;
    }
}
