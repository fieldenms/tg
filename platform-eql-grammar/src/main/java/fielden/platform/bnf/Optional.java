package fielden.platform.bnf;

public record Optional(Term term, TermMetadata metadata) implements Notation {

    public Optional(Term term) {
        this(term, TermMetadata.EMPTY_METADATA);
    }

    @Override
    public <V> Optional annotate(TermMetadata.Key<V> key, V value) {
        return new Optional(term, TermMetadata.merge(metadata(), key, value));
    }

}
