package fielden.platform.bnf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Metadata {

    public static final Metadata EMPTY_METADATA = new Metadata();

    /** Label for a term. */
    public static final Key<String> LABEL = new Key<>("label");
    /** <i>List label</i> for a term, specific to <a href="https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels">ANTLR</a>. */
    public static final Key<String> LIST_LABEL = new Key<>("list_label");

    private final Map<Key<?>, Object> map;

    public Metadata(final Map<Key<?>, Object> map) {
        this.map = Map.copyOf(map);
    }

    public Metadata() {
        this.map = Map.of();
    }

    public <T> Optional<T> get(final Key<T> key) {
        return Optional.ofNullable((T) map.get(key));
    }

    public static <V> Metadata merge(final Metadata metadata, final Key<V> key, final V value) {
        var builder = new Builder();
        metadata.map.forEach(builder::add);
        builder.add(key, value);
        return builder.build();
    }

    public static final class Key<T> {
        /**
         * This key's name. Doesn't affect its identity (there might be multiple keys with the same name).
         */
        private final String name;

        private Key(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static Builder builder() {
        return new Builder();
    }

    private static final class Builder {
        private final Map<Key<?>, Object> map = new HashMap<>();

        public Metadata build() {
            return new Metadata(map);
        }

        public <T> Builder add(final Key<? extends T> key, final T value) {
            map.put(key, value);
            return this;
        }
    }

}
