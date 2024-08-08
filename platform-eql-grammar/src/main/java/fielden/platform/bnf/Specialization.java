package fielden.platform.bnf;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Specialization is a special case of derivation where the right-hand side contains alternatives that consist of a single
 * variable.
 *
 * <pre>
 *     Statement := Conditional | Assignment | MethodCall
 * </pre>
 */
public record Specialization(Variable lhs, List<Variable> specializers) implements Rule {

    @Override
    public Alternation rhs() {
        return new Alternation(specializers);
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs, specializers.stream().map(Variable::name).collect(joining(" | ")));
    }

}

