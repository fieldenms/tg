package fielden.platform.bnf;

import java.util.function.Function;

public record ZeroOrMore(Term term, Metadata metadata) implements Quantifier {

    public ZeroOrMore(final Term term) {
        this(term, Metadata.EMPTY_METADATA);
    }

    @Override
    public <V> ZeroOrMore annotate(final Metadata.Key<V> key, final V value) {
        return new ZeroOrMore(term, Metadata.merge(metadata(), key, value));
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

}
