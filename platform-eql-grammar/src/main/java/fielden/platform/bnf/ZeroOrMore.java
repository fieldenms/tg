package fielden.platform.bnf;

public record ZeroOrMore(Term term, TermMetadata metadata) implements Notation {

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

}
