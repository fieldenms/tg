package ua.com.fielden.platform.expression.lexer.date;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Pre-final state for the date and time portion of the date literal.
 * It expects a single quote to complete recognition.
 *
 * @author TG Team
 *
 */
public class State24_PreFinal_for_Date_and_Time_Portion extends AbstractState {

    public State24_PreFinal_for_Date_and_Time_Portion() {
	super("S24", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) { // give it a chance with accidental white spaces after the time portion
	    return this;
	} else if (symbol == '\'') { // successful recognition
	    return getAutomata().getState("FINAL");
	} else if (symbol == ExpressionLexer.EOF) {
	    throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
	} else  {
	    return getAutomata().getState("GRAVE");
	}
    }

}
