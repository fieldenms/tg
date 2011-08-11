package ua.com.fielden.platform.expression.lexer.string;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising STRING token of the expression language.
 *
 * @author TG Team
 *
 */
public class StringTokenAutomata extends BaseNonDeterministicAutomata {

    public StringTokenAutomata() {
	super(EgTokenCategory.STRING, TEXT_POST_PROCESSING.TRIM, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "STRING token automata";
    }

}
