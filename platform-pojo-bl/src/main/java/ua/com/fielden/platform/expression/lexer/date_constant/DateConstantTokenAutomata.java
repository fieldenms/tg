package ua.com.fielden.platform.expression.lexer.date_constant;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising DATE_CONST token of the expression language.
 *
 * @author TG Team
 *
 */
public class DateConstantTokenAutomata extends BaseNonDeterministicAutomata {

    public DateConstantTokenAutomata() {
	super(EgTokenCategory.DATE_CONST, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "DATE_CONST token automata";
    }

}
