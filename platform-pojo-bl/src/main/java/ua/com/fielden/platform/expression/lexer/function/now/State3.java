package ua.com.fielden.platform.expression.lexer.function.now;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

/**
 * Final state.
 *
 * @author TG Team
 *
 */
public class State3 extends AbstractState {

    public State3() {
	super("S3", true);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
	if (isWhiteSpace(symbol)) {
	    return this;
	} else	if (String.valueOf(symbol).matches("[^\\p{L}^\\d^.]") || symbol == ExpressionLexer.EOF) {
	    throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
	} else {
	    // this would happen only if string NOW would continue with some characters that is valid for name token e.g. NOWed, making it a valid name rather than keyword NOW
	    // thus move to non-final state representing a dead-end for this automata
	    return getAutomata().getState("S4");
	}
    }

    public static void main(final String[] args) {
	System.out.println(String.valueOf('b').matches("[^\\p{L}^\\d]"));
	System.out.println(String.valueOf('1').matches("[^\\p{L}^\\d]"));
	System.out.println(String.valueOf('.').matches("[^\\p{L}^\\d^.]"));
	System.out.println(String.valueOf(' ').matches("[^\\p{L}^\\d^.]"));
    }

}
