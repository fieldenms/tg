package fielden.platform.bnf;

import java.util.function.Function;

public record Derivation(Variable lhs, Alternation rhs) implements Rule {

    public Rule recMap(final Function<? super Term, ? extends Term> mapper) {
        return new Derivation(lhs, rhs.recMap(mapper));
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs.name(), rhs);
    }

}

