package fielden.platform.bnf;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;

public record BNF(
        Set<Terminal> terminals,
        Set<Variable> variables,
        Variable start,
        Set<Rule> rules
) {

    public BNF(final Set<Terminal> terminals, final Set<Variable> variables, final Variable start, final Set<Rule> rules) {
        this.terminals = unmodifiableSet(new LinkedHashSet<>(terminals));
        this.variables = unmodifiableSet(new LinkedHashSet<>(variables));
        this.start = start;
        this.rules = unmodifiableSet(new LinkedHashSet<>(rules));
    }

    /** @return stream of all grammar symbols */
    public Stream<Symbol> symbols() {
        return Stream.concat(terminals.stream(), variables.stream());
    }

    @Override
    public String toString() {
        return "<Σ=" + terminals + ", Γ=" + variables + ", ε=" + start + ", R=" + rules + ">";
    }

}
