package fielden.platform.bnf;

import java.util.function.Function;

public record Optional(Term term, TermMetadata metadata) implements Quantifier {

    public Optional(Term term) {
        this(term, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> Optional annotate(TermMetadata.Key<V> key, V value) {
        return new Optional(term, TermMetadata.merge(metadata(), key, value));
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
    public String toString() {
        return "{%s}?".formatted(term);
    }

}
