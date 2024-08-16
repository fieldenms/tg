package fielden.platform.bnf;

import java.util.Objects;
import java.util.function.Function;

public record ZeroOrMore(Term term, Metadata metadata) implements Quantifier {

    public ZeroOrMore(final Term term) {
        this(term, Metadata.EMPTY_METADATA);
    }

    @Override
    public ZeroOrMore annotate(final Metadata.Annotation annotation) {
        return new ZeroOrMore(term, Metadata.merge(metadata(), annotation));
    }

    @Override
    public ZeroOrMore normalize() {
        return new ZeroOrMore(term);
    }

    @Override
    public ZeroOrMore recMap(final Function<? super Term, ? extends Term> mapper) {
        return new ZeroOrMore(term.recMap(mapper), metadata);
    }

    @Override
    public ZeroOrMore map(final Function<? super Term, ? extends Term> mapper) {
        return new ZeroOrMore(mapper.apply(term), metadata);
    }

    @Override
    public String toString() {
        return "{%s}*".formatted(term);
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
