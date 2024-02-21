package fielden.platform.bnf;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public final class Derivation implements Rule {

    private final Variable lhs;
    private final List<Sequence> rhs;

    public Derivation(Variable lhs, List<Sequence> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Stream<Sequence> rhs() {
        return rhs.stream();
    }

    @Override
    public Variable lhs() {
        return lhs;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
                (obj instanceof Derivation that &&
                        Objects.equals(this.lhs, that.lhs) &&
                        Objects.equals(this.rhs, that.rhs));
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs);
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(
                lhs.name(),
                rhs.stream().map(terms -> terms.stream().map(Term::toString).collect(joining(" ")))
                        .collect(joining(" | ")));
    }
}

