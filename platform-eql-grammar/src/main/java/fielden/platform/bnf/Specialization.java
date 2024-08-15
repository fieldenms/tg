package fielden.platform.bnf;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.joining;

/**
 * Specialization is a special case of derivation where the right-hand side contains alternatives that consist of a single
 * variable.
 *
 * <pre>
 *     Statement := Conditional | Assignment | MethodCall
 * </pre>
 */
public record Specialization(Variable lhs, List<Variable> specializers, Metadata metadata) implements Rule {

    public Specialization(final Variable lhs, final List<Variable> specializers) {
        this(lhs, ImmutableList.copyOf(specializers), Metadata.EMPTY_METADATA);
    }

    @Override
    public Alternation rhs() {
        return new Alternation(specializers);
    }

    public Specialization mapRhs(final Function<? super Variable, Variable> fn) {
        return new Specialization(lhs, specializers.stream().map(fn).collect(toImmutableList()), metadata);
    }

    @Override
    public Specialization annotate(final Metadata.Annotation annotation) {
        return new Specialization(lhs, specializers, Metadata.merge(metadata, annotation));
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs, specializers.stream().map(Variable::name).collect(joining(" | ")));
    }

}

