package ua.com.fielden.platform.expression.lexer.integer;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising INTEGER token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class IntegerTokenAutomata extends BaseNonDeterministicAutomata {

    public IntegerTokenAutomata() {
        super(EgTokenCategory.INT, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "INTEGER token automata";
    }

}
