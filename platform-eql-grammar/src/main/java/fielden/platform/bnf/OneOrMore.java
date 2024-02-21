package fielden.platform.bnf;

public record OneOrMore(Term term, TermMetadata metadata) implements Notation {

    public OneOrMore(Term term) {
        this(term, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> OneOrMore annotate(TermMetadata.Key<V> key, V value) {
        return new OneOrMore(term, TermMetadata.merge(metadata(), key, value));
    }

}
