package fielden.platform.bnf;

import ua.com.fielden.platform.utils.StreamUtils;

import java.util.stream.Stream;

/**
 * A rule is what makes up a grammar.
 */
public sealed interface Rule permits Derivation, Specialization {

    /**
     * The left-hand side of the rule, which is a variable defined by the rule.
     */
    Variable lhs();

    /**
     * The right-hand side of the rule, which is the rule's body. This is the most general body form - an alternation.
     * For simple rules there will be a single alternative.
     */
    Alternation rhs();

    default Metadata metadata() {
        return Metadata.EMPTY_METADATA;
    }

    /**
     * Produces a new rule that is equal to this rule but with an additional annotation specified by the given key and value.
     */
    Rule annotate(final Metadata.Annotation annotation);

    /**
     * Tests whether a rule has a single alternative on its right-hand side.
     */
    static boolean isSingleAltRule(final Rule rule) {
        return rule.rhs().options().size() == 1;
    }

    static Stream<Symbol> rhsSymbols(final Rule rule) {
        return rule.rhs().flatten().mapMulti(StreamUtils.typeFilter(Symbol.class));
    }

}
