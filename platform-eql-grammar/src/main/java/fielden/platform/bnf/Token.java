package fielden.platform.bnf;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * A token is a parameterised terminal.
 */
public record Token(String name, TermMetadata metadata, List<? extends Parameter> parameters) implements Terminal, Parameterised {

    public Token(String name, TermMetadata metadata, List<? extends Parameter> parameters) {
        this.name = name;
        this.metadata = metadata;
        this.parameters = List.copyOf(parameters);
    }

    public Token(Terminal terminal, List<? extends Parameter> parameters) {
        this(terminal.name(), terminal.metadata(), parameters);
    }

    public Token(String name, TermMetadata metadata, Parameter... parameters) {
        this(name, metadata, Arrays.asList(parameters));
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    @Override
    public Token normalize() {
        return new Token(name, TermMetadata.EMPTY_METADATA, parameters);
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

}
