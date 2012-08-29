package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Pre-final state for the date portion of the date literal. It expects a single quote to complete recognition, a space to move to the time portion or something else to fail
 * recognition by moving a non-final state.
 *
 * @author TG Team
 *
 */
public class State14_PreFinal_for_Date_Portion extends AbstractState {

    public State14_PreFinal_for_Date_Portion() {
	super("S14", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (symbol == '\'') { // successful recognition
	    return getAutomata().getState("FINAL");
	} else if (isWhiteSpace(symbol)) {
	    return getAutomata().getState("S15");
	} else if (symbol == ExpressionLexer.EOF) {
	    throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
	} else {
	    return getAutomata().getState("GRAVE");
	}
    }

}
