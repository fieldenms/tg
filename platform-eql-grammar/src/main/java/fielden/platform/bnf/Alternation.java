package fielden.platform.bnf;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Represents an alternative choice among several terms. The BNF notation for alternation is {@code term1 | term2}.
 * <p>
 * Representation of options (choices) as sequences enables options that consist of several terms.
 * <pre>
 *     column | column AS alias
 *     ; is viewed as
 *     (column) | (column AS alias)
 * </pre>
 */
public final class Alternation implements Notation {

    private final List<Term> options;
    private final TermMetadata metadata;

    public Alternation(final List<? extends Term> options, final TermMetadata metadata) {
        this.options = List.copyOf(options);
        this.metadata = metadata;
    }

    public Alternation(final List<? extends Term> options) {
        this(options, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> Alternation annotate(final TermMetadata.Key<V> key, final V value) {
        return new Alternation(options, TermMetadata.merge(metadata(), key, value));
    }

    @Override
    public Alternation normalize() {
        return metadata == TermMetadata.EMPTY_METADATA ? this : new Alternation(options);
    }

    @Override
    public Alternation recMap(final Function<? super Term, ? extends Term> mapper) {
        return new Alternation(options.stream().map(seq -> seq.recMap(mapper)).toList(), metadata);
    }

    @Override
    public Stream<Term> flatten() {
        return options.stream().flatMap(Term::flatten);
    }

    @Override
    public String toString() {
        return options().stream().map(Term::toString).collect(joining(" | "));
    }

    public List<Term> options() {
        return options;
    }

    @Override
    public TermMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Alternation that &&
                              Objects.equals(this.options, that.options) &&
                              Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options, metadata);
    }

}
