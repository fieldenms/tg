package fielden.platform.bnf;

import java.util.Objects;
import java.util.function.Function;

public record OneOrMore(Term term, Metadata metadata) implements Quantifier {

    public OneOrMore(final Term term) {
        this(term, Metadata.EMPTY_METADATA);
    }

    public OneOrMore annotate(final Metadata.Annotation annotation) {
        return new OneOrMore(term, Metadata.merge(metadata(), annotation));
    }

    @Override
    public OneOrMore normalize() {
        return new OneOrMore(term);
    }

    @Override
    public OneOrMore recMap(final Function<? super Term, ? extends Term> mapper) {
        return new OneOrMore(term.recMap(mapper), metadata);
    }

    @Override
    public OneOrMore map(final Function<? super Term, ? extends Term> mapper) {
        return new OneOrMore(mapper.apply(term), metadata);
    }

    @Override
    public String toString() {
        return "{%s}+".formatted(term);
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
