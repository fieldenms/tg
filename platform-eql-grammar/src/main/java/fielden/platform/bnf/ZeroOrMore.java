package fielden.platform.bnf;

import java.util.function.Function;

public record ZeroOrMore(Term term, TermMetadata metadata) implements Quantifier {

    public ZeroOrMore(Term term) {
        this(term, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> ZeroOrMore annotate(TermMetadata.Key<V> key, V value) {
        return new ZeroOrMore(term, TermMetadata.merge(metadata(), key, value));
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
    public String toString() {
        return "{%s}*".formatted(term);
    }

}
