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

    default Metadata metadata() {
        return Metadata.EMPTY_METADATA;
    }

    /**
     * Produces a new term that is equal to this term but with an additional annotation specified by the given key and value.
     */
    <V> Term annotate(final Metadata.Key<V> key, final V value);

    /**
     * Completely and recursively flattens this term's structure.
     * If this term is a nested structure of other terms, returns all of them by recursively flattening them first.
     * Otherwise, this term is an atom, and a single-element stream with this term is returned.
     */
    Stream<Term> flatten();

    /**
     * Recursive map.
     */
    Term recMap(final Function<? super Term, ? extends Term> mapper);

    /**
     * Map each term in this structure (non-recursively).
     * If this term is a nested structure of other terms, map each term without recursing deeper.
     * Otherwise, this term is an atom, and the function is applied directly to it.
     */
    Term map(Function<? super Term, ? extends Term> mapper);

}
