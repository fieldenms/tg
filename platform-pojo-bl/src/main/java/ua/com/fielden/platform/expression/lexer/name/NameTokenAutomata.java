package ua.com.fielden.platform.expression.lexer.name;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising NAME token of the expression language.
 *
 * @author TG Team
 *
 */
public class NameTokenAutomata extends BaseNonDeterministicAutomata {

    public NameTokenAutomata() {
	super(EgTokenCategory.NAME, TEXT_POST_PROCESSING.REMOVE_WS, new State0(), new State1(), new State2());
    }

    @Override
    public String toString() {
        return "NAME token automata";
    }

}
