package fielden.platform.bnf;

import java.util.Arrays;
import java.util.List;

/**
 * A token is a parameterised terminal.
 */
public record Token(String name, List<? extends Parameter> parameters) implements Terminal, Parameterised {

    public Token(String name, List<? extends Parameter> parameters) {
        this.name = name;
        this.parameters = List.copyOf(parameters);
    }

    public Token(Terminal terminal, List<? extends Parameter> parameters) {
        this(terminal.name(), parameters);
    }

    public Token(String name, Parameter... parameters) {
        this(name, Arrays.asList(parameters));
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

}
