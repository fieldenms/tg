package ua.com.fielden.platform.expression.lexer.and;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising AND token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class AndTokenAutomata extends BaseNonDeterministicAutomata {

    public AndTokenAutomata() {
        super(EgTokenCategory.AND, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "AND token automata";
    }

}
