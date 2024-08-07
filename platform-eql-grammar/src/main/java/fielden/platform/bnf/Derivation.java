package fielden.platform.bnf;

import java.util.function.Function;

/**
 * Derivation is the most general grammar rule. It consists of a variable (left-hand side) that is defined in terms of
 * one or more alternatives (right-hand side).
 * <pre>
 *     ; 1 alternative
 *     Column := string
 *     ; 2 alternatives
 *     Column := string | string AS Alias
 * </pre>
 */
public record Derivation(Variable lhs, Alternation rhs) implements Rule {

    public Rule recMap(final Function<? super Term, ? extends Term> mapper) {
        return new Derivation(lhs, rhs.recMap(mapper));
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(lhs.name(), rhs);
    }

}

