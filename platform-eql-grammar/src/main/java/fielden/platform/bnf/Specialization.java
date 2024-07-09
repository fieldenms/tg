package fielden.platform.bnf;

import java.util.List;

import static java.util.stream.Collectors.joining;

public record Specialization(Variable lhs, List<Variable> specializers) implements Rule {

    @Override
    public Alternation rhs() {
        return new Alternation(specializers.stream().map(Sequence::new).toList());
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs, specializers.stream().map(Variable::name).collect(joining(" | ")));
    }

}

