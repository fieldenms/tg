package ua.com.fielden.platform.expression.lexer.or;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising OR token of the expression language.
 *
 * @author TG Team
 *
 */
public class OrTokenAutomata extends BaseNonDeterministicAutomata {

    public OrTokenAutomata() {
	super(EgTokenCategory.OR, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
	return "OR token automata";
    }

}
