package ua.com.fielden.platform.expression.lexer;

import ua.com.fielden.platform.expression.ILexemeCategory;
import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NonDeterministicAutomata;

public abstract class BaseNonDeterministicAutomata extends NonDeterministicAutomata {

    public final ILexemeCategory lexemeCat;

    public BaseNonDeterministicAutomata(final ILexemeCategory tokenCat, final TEXT_POST_PROCESSING postProcessingAction, final AbstractState initState, final AbstractState... states) {
        super(postProcessingAction, initState, states);
        this.lexemeCat = tokenCat;
    }

}
