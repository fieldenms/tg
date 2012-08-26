package ua.com.fielden.platform.expression.lexer.case_when.when;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising WHEN token of the expression language.
 *
 * @author TG Team
 *
 */
public class WhenTokenAutomata extends BaseNonDeterministicAutomata {

    public WhenTokenAutomata() {
	super(EgTokenCategory.WHEN, TEXT_POST_PROCESSING.REMOVE_WS, new State0(), new State1(), new State2(), new State3(), new State4(), new State5(), new State6());
    }

    @Override
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
	final String result = super.recognisePartiallyFromStart(input, posInOriginalSequence);
	if (result.endsWith("(")) {
	    return result.substring(0, result.length() - 1).toUpperCase();
	}
	return result.substring(0, result.length()).toUpperCase();
    }

    @Override
    public String toString() {
	return "WHEN token automata";
    }

}
