package ua.com.fielden.platform.expression.lexer.function.hours;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising HOURS token of the expression language.
 *
 * @author TG Team
 *
 */
public class HoursTokenAutomata extends BaseNonDeterministicAutomata {

    public HoursTokenAutomata() {
	super(EgTokenCategory.HOURS, TEXT_POST_PROCESSING.REMOVE_WS, new State0(), new State1(), new State2(), new State3(), new State4(), new State5(), new State6());
    }

    @Override
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
	final String result = super.recognisePartiallyFromStart(input, posInOriginalSequence);
	setCharsRecognised(getCharsRecognised() - 1); // need to decrease by one as the LPAREN after MONTHS was read, but should not be recognised as part of the token
	return result.substring(0, result.length() - 1).toUpperCase(); // need to remove the left parenthesis
    }

    @Override
    public String toString() {
	return "HOURS token automata";
    }

}
