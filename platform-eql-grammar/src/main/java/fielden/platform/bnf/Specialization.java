package fielden.platform.bnf;

import java.util.List;
import java.util.function.Function;

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

    public Specialization mapRhs(final Function<? super Variable, Variable> fn) {
        return new Specialization(lhs, specializers.stream().map(fn).toList());
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs, specializers.stream().map(Variable::name).collect(joining(" | ")));
    }

}

