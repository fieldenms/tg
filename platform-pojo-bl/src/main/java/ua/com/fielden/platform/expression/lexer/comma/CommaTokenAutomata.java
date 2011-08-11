package ua.com.fielden.platform.expression.lexer.comma;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising COMMA token of the expression language.
 *
 * @author TG Team
 *
 */
public class CommaTokenAutomata extends BaseNonDeterministicAutomata {

    public CommaTokenAutomata() {
	super(EgTokenCategory.COMMA, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
        return "COMMA token automata";
    }

}
