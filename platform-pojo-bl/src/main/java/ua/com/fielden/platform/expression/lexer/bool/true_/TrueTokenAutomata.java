package ua.com.fielden.platform.expression.lexer.bool.true_;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/// NDA for recognising TRUE token of the expression language.
///
public class TrueTokenAutomata extends BaseNonDeterministicAutomata {

    public TrueTokenAutomata() {
        super(EgTokenCategory.TRUE, TEXT_POST_PROCESSING.REMOVE_WS, new State0(), new State1(), new State2(), new State3(), new State4(), new State5DeadEnd());
    }

    @Override
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
        final String result = super.recognisePartiallyFromStart(input, posInOriginalSequence);
        return result.toLowerCase();
    }

    @Override
    public String toString() {
        return "TRUE token automata";
    }

}