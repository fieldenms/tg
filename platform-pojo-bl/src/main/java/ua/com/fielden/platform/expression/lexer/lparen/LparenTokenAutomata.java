package ua.com.fielden.platform.expression.lexer.lparen;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.NonDeterministicAutomata.TEXT_POST_PROCESSING;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising LPAREN token of the expression language.
 *
 * @author TG Team
 *
 */
public class LparenTokenAutomata extends BaseNonDeterministicAutomata {

    public LparenTokenAutomata() {
	super(EgTokenCategory.LPAREN, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
        return "LPAREN token automata";
    }

}
