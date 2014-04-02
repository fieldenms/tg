package ua.com.fielden.platform.expression.lexer.decimal;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising DECIMAL token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class DecimalTokenAutomata extends BaseNonDeterministicAutomata {

    public DecimalTokenAutomata() {
        super(EgTokenCategory.DECIMAL, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2(), new State3());
    }

    @Override
    public String toString() {
        return "DECIMAL token automata";
    }

}
