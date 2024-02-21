package fielden.platform.bnf;

/**
 * The most general grammar term.
 */
public sealed interface Term permits LabeledTerm, Notation, Sequence, Symbol {
}
