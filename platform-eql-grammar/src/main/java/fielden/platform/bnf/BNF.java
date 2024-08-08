package fielden.platform.bnf;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;

/**
 * Represents a grammar in the <a href="https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form">Backus-Naur form</a>.
 */
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

    public Optional<Rule> findRuleFor(final Variable variable) {
        return rules.stream().filter(rule -> rule.lhs().name().equals(variable.name())).findFirst();
    }

    public Rule getRuleFor(final Variable variable) {
        return findRuleFor(variable).orElseThrow(() -> new BnfException("No such rule: %s".formatted(variable.name())));
    }

    @Override
    public String toString() {
        return "<Σ=" + terminals + ", Γ=" + variables + ", ε=" + start + ", R=" + rules + ">";
    }

}
