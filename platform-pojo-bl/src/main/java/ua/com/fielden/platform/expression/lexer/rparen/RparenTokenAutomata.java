package ua.com.fielden.platform.expression.lexer.rparen;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising RPAREN token of the expression language.
 *
 * @author TG Team
 *
 */
public class RparenTokenAutomata extends BaseNonDeterministicAutomata {

    public RparenTokenAutomata() {
	super(EgTokenCategory.RPAREN, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
        return "RPAREN token automata";
    }

}
