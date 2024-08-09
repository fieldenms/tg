package fielden.platform.bnf;

import java.util.function.Function;

public record Optional(Term term, Metadata metadata) implements Quantifier {

    public Optional(final Term term) {
        this(term, Metadata.EMPTY_METADATA);
    }

    @Override
    public <V> Optional annotate(final Metadata.Key<V> key, final V value) {
        return new Optional(term, Metadata.merge(metadata(), key, value));
    }

    @Override
    public Optional normalize() {
        return new Optional(term);
    }

    @Override
    public Optional recMap(final Function<? super Term, ? extends Term> mapper) {
        return new Optional(term.recMap(mapper), metadata);
    }

    @Override
    public Optional map(final Function<? super Term, ? extends Term> mapper) {
        return new Optional(mapper.apply(term), metadata);
    }

    @Override
    public String toString() {
        return "{%s}?".formatted(term);
    }

}
