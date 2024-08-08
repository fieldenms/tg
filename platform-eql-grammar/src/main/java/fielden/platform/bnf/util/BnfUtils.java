package fielden.platform.bnf.util;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.Rule;
import fielden.platform.bnf.Symbol;

import java.util.stream.Stream;

public final class BnfUtils {

    /**
     * Finds all occurences of a symbol in the right-hand side of all grammar rules.
     */
    public static Stream<Rule> findRhsOccurences(final Symbol symbol, final BNF bnf) {
        return bnf.rules().stream().filter(rule -> occursInRhs(symbol, rule));
    }

    /**
     * Tests whether a symbol occurs in the right-hand side of a rule.
     */
    public static boolean occursInRhs(final Symbol symbol, final Rule rule) {
        return rule.rhs().flatten().anyMatch(term -> term instanceof Symbol sym && sym.name().equals(symbol.name()));
    }

    private BnfUtils() {}

}
