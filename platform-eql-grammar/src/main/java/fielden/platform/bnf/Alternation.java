package fielden.platform.bnf;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public record Alternation(List<Sequence> options, TermMetadata metadata) implements Notation {

    public Alternation(final List<Sequence> options, final TermMetadata metadata) {
        this.options = List.copyOf(options);
        this.metadata = metadata;
    }

    public Alternation(List<Sequence> options) {
        this(options, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> Alternation annotate(TermMetadata.Key<V> key, V value) {
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
        return options.stream().flatMap(Sequence::flatten);
    }

    @Override
    public String toString() {
        return options().stream().map(Term::toString).collect(joining(" | "));
    }

}