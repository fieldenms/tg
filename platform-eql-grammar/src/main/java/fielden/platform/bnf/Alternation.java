package fielden.platform.bnf;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
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
    private final Metadata metadata;

    public Alternation(final List<? extends Term> options, final Metadata metadata) {
        this.options = ImmutableList.copyOf(options);
        this.metadata = metadata;
    }

    public Alternation(final List<? extends Term> options) {
        this(options, Metadata.EMPTY_METADATA);
    }

    @Override
    public Alternation annotate(final Metadata.Annotation annotation) {
        return new Alternation(options, Metadata.merge(metadata(), annotation));
    }

    @Override
    public Alternation normalize() {
        return metadata == Metadata.EMPTY_METADATA ? this : new Alternation(options);
    }

    @Override
    public Alternation recMap(final Function<? super Term, ? extends Term> mapper) {
        return new Alternation(options.stream().map(seq -> seq.recMap(mapper)).collect(toImmutableList()), metadata);
    }

    @Override
    public Alternation map(final Function<? super Term, ? extends Term> mapper) {
        return new Alternation(options.stream().map(mapper).collect(toImmutableList()), metadata);
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
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof Alternation that &&
                              Objects.equals(this.options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options);
    }

}
