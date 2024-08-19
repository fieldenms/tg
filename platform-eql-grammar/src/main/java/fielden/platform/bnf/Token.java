package fielden.platform.bnf;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * A token is a parameterised terminal.
 */
public record Token(String name, Metadata metadata, List<? extends Parameter> parameters) implements Terminal, Parameterised {

    public Token(final String name, final Metadata metadata, final List<? extends Parameter> parameters) {
        this.name = name;
        this.metadata = metadata;
        this.parameters = ImmutableList.copyOf(parameters);
    }

    public Token(final Terminal terminal, final List<? extends Parameter> parameters) {
        this(terminal.name(), terminal.metadata(), parameters);
    }

    public Token(final String name, final Metadata metadata, final Parameter... parameters) {
        this(name, metadata, ImmutableList.copyOf(parameters));
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    @Override
    public Token normalize() {
        return new Token(name, Metadata.EMPTY_METADATA, parameters);
    }

    public Token stripParameters() {
        if (hasParameters())
            return new Token(name, metadata);
        return this;
    }

    @Override
    public String toString() {
        return "%s%s".formatted(
                name,
                parameters.isEmpty() ? "" : parameters.stream().map(Parameter::toString).collect(joining(",", "(", ")")));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof Token that && name.equals(that.name) && parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }

    @Override
    public Token annotate(final Metadata.Annotation annotation) {
        return new Token(name, Metadata.merge(metadata, annotation), parameters);
    }

}
