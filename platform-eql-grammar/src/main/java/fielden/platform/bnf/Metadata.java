package fielden.platform.bnf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Metadata {

    public static final Metadata EMPTY_METADATA = new Metadata();

    /** Label for a term. */
    public record Label (String label) implements Annotation {}
    public static Label label(final String label) {
        return new Label(label);
    }

    /** <i>List label</i> for a term, specific to <a href="https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels">ANTLR</a>. */
    public record ListLabel (String label) implements Annotation {}
    public static ListLabel listLabel(final String label) {
        return new ListLabel(label);
    }

    /** Signals that the annotated rule should be inlined during transformation. */
    public static final class Inline implements Annotation {
        private static final Inline INSTANCE = new Inline();
        private Inline() {}
    }
    public static Inline inline() {
        return Inline.INSTANCE;
    }

    private final Map<Class<? extends Annotation>, Annotation> map;

    public Metadata(final Map<Class<? extends Annotation>, Annotation> map) {
        this.map = Map.copyOf(map);
    }

    public Metadata() {
        this.map = Map.of();
    }

    public <A extends Annotation> Optional<A> get(final Class<A> annotationType) {
        return Optional.ofNullable((A) map.get(annotationType));
    }

    public boolean has(final Class<? extends Annotation> annotationType) {
        return map.containsKey(annotationType);
    }

    public static Metadata merge(final Metadata metadata, final Annotation annotation) {
        var builder = new Builder();
        metadata.map.values().forEach(builder::add);
        builder.add(annotation);
        return builder.build();
    }

    public interface Annotation {}

    private static Builder builder() {
        return new Builder();
    }

    private static final class Builder {
        private final Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();

        public Metadata build() {
            return new Metadata(map);
        }

        public Builder add(final Annotation annotation) {
            map.put(annotation.getClass(), annotation);
            return this;
        }
    }

}
