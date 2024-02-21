package fielden.platform.bnf;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public record Specialization(Variable lhs, List<Variable> specializers) implements Rule {

    @Override
    public Stream<Sequence> rhs() {
        return specializers.stream().map(Sequence::new);
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs, specializers.stream().map(Variable::name).collect(joining(" | ")));
    }

}

