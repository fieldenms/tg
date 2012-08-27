package ua.com.fielden.platform.expression.lexer.less;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising LT token of the expression language.
 *
 * @author TG Team
 *
 */
public class LessTokenAutomata extends BaseNonDeterministicAutomata {

    public LessTokenAutomata() {
	super(EgTokenCategory.LT, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
	return "LT token automata";
    }

}
