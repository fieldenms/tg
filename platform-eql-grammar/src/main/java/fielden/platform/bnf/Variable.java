package fielden.platform.bnf;

/**
 * A variable is a nonterminal symbol.
 */
public non-sealed interface Variable extends Symbol {

    @Override
    default <V> Variable annotate(TermMetadata.Key<V> key, V value) {
        final var newMetadata = TermMetadata.merge(metadata(), key, value);
        final String name = name();

        return new Variable() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public TermMetadata metadata() {
                return newMetadata;
            }
        };
    }

}
