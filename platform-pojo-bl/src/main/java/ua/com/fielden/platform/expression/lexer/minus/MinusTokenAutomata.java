package ua.com.fielden.platform.expression.lexer.minus;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising MINUS token of the expression language.
 *
 * @author TG Team
 *
 */
public class MinusTokenAutomata extends BaseNonDeterministicAutomata {

    public MinusTokenAutomata() {
	super(EgTokenCategory.MINUS, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
        return "MINUS token automata";
    }

}
