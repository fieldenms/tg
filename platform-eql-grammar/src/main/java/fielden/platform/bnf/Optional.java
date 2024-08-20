package fielden.platform.bnf;

import java.util.Objects;
import java.util.function.Function;

public record Optional(Term term, Metadata metadata) implements Quantifier {

    public Optional(final Term term) {
        this(term, Metadata.EMPTY_METADATA);
    }

    @Override
    public Optional annotate(final Metadata.Annotation annotation) {
        return new Optional(term, Metadata.merge(metadata(), annotation));
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

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Optional that && term.equals(that.term());
    }

    @Override
    public int hashCode() {
        return Objects.hash(term);
    }

}
