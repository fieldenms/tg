package ua.com.fielden.platform.expression.lexer.less_or_eq;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising LE token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class LessOrEqualTokenAutomata extends BaseNonDeterministicAutomata {

    public LessOrEqualTokenAutomata() {
        super(EgTokenCategory.LE, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "LE token automata";
    }

}
