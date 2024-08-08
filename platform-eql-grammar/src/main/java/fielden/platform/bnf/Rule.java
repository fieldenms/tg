package fielden.platform.bnf;

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

}
