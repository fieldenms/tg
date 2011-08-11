package ua.com.fielden.platform.expression.lexer.plus;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising PLUS token of the expression language.
 *
 * @author TG Team
 *
 */
public class PlusTokenAutomata extends BaseNonDeterministicAutomata {

    public PlusTokenAutomata() {
	super(EgTokenCategory.PLUS, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
        return "PLUS token automata";
    }

}
