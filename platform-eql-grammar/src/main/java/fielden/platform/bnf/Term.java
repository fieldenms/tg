package fielden.platform.bnf;

/**
 * The most general grammar term.
 */
public sealed interface Term permits Notation, Sequence, Symbol {

    default TermMetadata metadata() {
        return TermMetadata.EMPTY_METADATA;
    }

    <V> Term annotate(TermMetadata.Key<V> key, V value);

}
