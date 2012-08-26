package ua.com.fielden.platform.expression.lexer.less;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising LESS token of the expression language.
 *
 * @author TG Team
 *
 */
public class LessTokenAutomata extends BaseNonDeterministicAutomata {

    public LessTokenAutomata() {
	super(EgTokenCategory.LESS, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
	return "LESS token automata";
    }

}
