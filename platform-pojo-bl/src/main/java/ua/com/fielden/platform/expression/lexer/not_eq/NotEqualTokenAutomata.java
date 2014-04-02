package ua.com.fielden.platform.expression.lexer.not_eq;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising NE token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class NotEqualTokenAutomata extends BaseNonDeterministicAutomata {

    public NotEqualTokenAutomata() {
        super(EgTokenCategory.NE, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "NE token automata";
    }

}
