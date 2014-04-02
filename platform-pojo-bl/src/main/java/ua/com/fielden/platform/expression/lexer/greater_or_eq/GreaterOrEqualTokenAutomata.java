package ua.com.fielden.platform.expression.lexer.greater_or_eq;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising GE token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class GreaterOrEqualTokenAutomata extends BaseNonDeterministicAutomata {

    public GreaterOrEqualTokenAutomata() {
        super(EgTokenCategory.GE, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "GE token automata";
    }

}
