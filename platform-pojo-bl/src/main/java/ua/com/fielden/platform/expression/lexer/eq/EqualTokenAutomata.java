package ua.com.fielden.platform.expression.lexer.eq;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising EQ token of the expression language.
 *
 * @author TG Team
 *
 */
public class EqualTokenAutomata extends BaseNonDeterministicAutomata {

    public EqualTokenAutomata() {
	super(EgTokenCategory.EQ, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
	return "EQ token automata";
    }

}
