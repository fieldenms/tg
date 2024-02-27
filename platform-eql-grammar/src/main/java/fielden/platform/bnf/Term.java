package fielden.platform.bnf;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The most general grammar term.
 */
public sealed interface Term permits Notation, Sequence, Symbol {

    /**
     * Returns this term in its normal form.
     * The normal form doesn't include any metadata, making it possible to correctly compare terms irrespective of their
     * metadata.
     */
    default Term normalize() {
        return this;
    }

    default TermMetadata metadata() {
        return TermMetadata.EMPTY_METADATA;
    }

    <V> Term annotate(TermMetadata.Key<V> key, V value);

    Stream<Term> flatten();

    /**
     * Recursive map.
     */
    Term recMap(Function<? super Term, ? extends Term> mapper);

}
