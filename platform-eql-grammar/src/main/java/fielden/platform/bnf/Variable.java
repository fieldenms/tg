package fielden.platform.bnf;

import java.util.Objects;

/**
 * A variable is a non-terminal symbol.
 */
public non-sealed interface Variable extends Symbol {

    @Override
    default Variable normalize() {
        return this;
    }

    @Override
    default <V> Variable annotate(final Metadata.Key<V> key, final V value) {
        final var newMetadata = Metadata.merge(metadata(), key, value);
        final String name = name();
        final Variable normal = normalize();

        return new Variable() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Variable normalize() {
                return normal;
            }

            @Override
            public Metadata metadata() {
                return newMetadata;
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, newMetadata);
            }
        };
    }

}
