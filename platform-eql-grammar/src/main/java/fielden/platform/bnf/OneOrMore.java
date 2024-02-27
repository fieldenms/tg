package fielden.platform.bnf;

import java.util.function.Function;

public record OneOrMore(Term term, TermMetadata metadata) implements Quantifier {

    public OneOrMore(Term term) {
        this(term, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> OneOrMore annotate(TermMetadata.Key<V> key, V value) {
        return new OneOrMore(term, TermMetadata.merge(metadata(), key, value));
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
    public String toString() {
        return "{%s}+".formatted(term);
    }

}
