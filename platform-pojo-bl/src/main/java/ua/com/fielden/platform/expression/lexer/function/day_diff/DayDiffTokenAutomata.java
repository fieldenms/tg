package ua.com.fielden.platform.expression.lexer.function.day_diff;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising DAY_DIFF token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class DayDiffTokenAutomata extends BaseNonDeterministicAutomata {

    public DayDiffTokenAutomata() {
        super(EgTokenCategory.DAY_DIFF, TEXT_POST_PROCESSING.REMOVE_WS, new State0(), new State1(), new State2(), new State3(), new State4(), new State5(), new State6(), new State7(), new State8(), new State9());
    }

    @Override
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
        final String result = super.recognisePartiallyFromStart(input, posInOriginalSequence);
        setCharsRecognised(getCharsRecognised() - 1); // need to decrease by one as the LPAREN after DAY_DIFF was read, but should not be recognised as part of the token
        return result.substring(0, result.length() - 1).toUpperCase(); // need to remove the left parenthesis
    }

    @Override
    public String toString() {
        return "DAY_DIFF token automata";
    }

}
