package ua.com.fielden.platform.expression.lexer.greater;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising GT token of the expression language.
 * 
 * @author TG Team
 * 
 */
public class GreaterTokenAutomata extends BaseNonDeterministicAutomata {

    public GreaterTokenAutomata() {
        super(EgTokenCategory.GT, TEXT_POST_PROCESSING.TRIM, new State0(), new State1());
    }

    @Override
    public String toString() {
        return "GT token automata";
    }

}
