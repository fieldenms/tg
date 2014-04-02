package ua.com.fielden.platform.expression.lexer.case_when.then_;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising THEN token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class ThenTokenAutomata extends BaseNonDeterministicAutomata {

    public ThenTokenAutomata() {
        super(EgTokenCategory.THEN, TEXT_POST_PROCESSING.REMOVE_WS, new State0(), new State1(), new State2(), new State3(), new State4(), new State5());
    }

    @Override
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
        final String result = super.recognisePartiallyFromStart(input, posInOriginalSequence);
        return result.substring(0, result.length()).toUpperCase();
    }

    @Override
    public String toString() {
        return "THEN token automata";
    }

}
