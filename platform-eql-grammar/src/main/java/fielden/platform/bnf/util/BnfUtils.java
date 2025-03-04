package fielden.platform.bnf.util;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.Rule;
import fielden.platform.bnf.Symbol;

import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toCollection;

public final class BnfUtils {

    /**
     * Finds all occurrences of a symbol in the right-hand side of all grammar rules.
     */
    public static Stream<Rule> findRhsOccurences(final Symbol symbol, final BNF bnf) {
        return bnf.rules().stream().filter(rule -> occursInRhs(symbol, rule));
    }

    /**
     * Tests whether a symbol occurs in any right-hand side of all grammar rules.
     */
    public static boolean occursInAnyRhs(final Symbol symbol, final BNF bnf) {
        return bnf.rules().stream().anyMatch(rule -> occursInRhs(symbol, rule));
    }

    /**
     * Tests whether a symbol occurs in the right-hand side of a rule.
     */
    public static boolean occursInRhs(final Symbol symbol, final Rule rule) {
        return rule.rhs().flatten().anyMatch(term -> term instanceof Symbol sym && sym.name().equals(symbol.name()));
    }

    /**
     * Counts all occurences of a symbol in the right-hand side of a rule.
     */
    public static long countRhsOccurrences(final Symbol symbol, final Rule rule) {
        return rule.rhs().flatten().filter(term -> term instanceof Symbol sym && sym.name().equals(symbol.name())).count();
    }

    /**
     * Transforms a grammar by removing all unused terminals, variables and rules associated with those variables.
     */
    public static final GrammarTransformer removeUnused = bnf -> {
        final var usedVars = bnf.variables().stream()
                // starting variable is always considered used
                .filter(var -> var.name().equals(bnf.start().name()) || occursInAnyRhs(var, bnf))
                .collect(toImmutableSet());
        final var usedTerminals = bnf.terminals().stream()
                .filter(terminal -> occursInAnyRhs(terminal, bnf))
                .collect(toImmutableSet());
        final var usedRules = bnf.rules().stream()
                .filter(rule -> usedVars.stream().anyMatch(usedVar -> usedVar.name().equals(rule.lhs().name())))
                .collect(toCollection(LinkedHashSet::new));
        return new BNF(usedTerminals, usedVars, bnf.start(), usedRules);
    };

    private BnfUtils() {}

}
