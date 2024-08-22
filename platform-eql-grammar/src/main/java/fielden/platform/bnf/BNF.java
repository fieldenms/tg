package fielden.platform.bnf;

import ua.com.fielden.platform.utils.StreamUtils;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

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

    public BNF addRule(final Rule rule) {
        final var newRules = new LinkedHashSet<Rule>();
        newRules.add(rule);
        rules.stream().filter(r -> !r.lhs().name().equals(rule.lhs().name())).forEach(newRules::add);
       return addVar(rule.lhs()).addSymbols(Rule.rhsSymbols(rule).toList()).updateRules(newRules);
    }

    private BNF updateRules(final Set<Rule> rules) {
        return new BNF(terminals,
                       variables,
                       start,
                       rules);
    }

    public BNF addSymbols(final Iterable<? extends Symbol> symbols) {
        BNF tmpBnf = this;
        for (final Symbol symbol : symbols) {
            switch (symbol) {
                case Terminal terminal -> tmpBnf = tmpBnf.addTerminal(terminal);
                case Variable variable -> tmpBnf = tmpBnf.addVar(variable);
            }
        }
        return tmpBnf;
    }

    public BNF removeVars(final Iterable<? extends Variable> variables) {
        BNF tmpBnf = this;
        for (final Variable variable : variables) {
            tmpBnf = tmpBnf.removeVar(variable);
        }
        return tmpBnf;
    }

    public BNF removeVar(final Variable variable) {
        if (variable.name().equals(start.name())) {
            throw new BnfException("Can't remove the start variable [%s]".formatted(variable.name()));
        }
        if (!contains(variable)) {
            throw new BnfException("No such variable [%s]".formatted(variable.name()));
        }
        return new BNF(terminals,
                       variables.stream().filter(var -> !var.name().equals(variable.name())).collect(toImmutableSet()),
                       start,
                       rules.stream().filter(rule -> !rule.lhs().name().equals(variable.name())).collect(toCollection(LinkedHashSet::new)));
    }

    public BNF addVars(final Iterable<? extends Variable> variables) {
        BNF tmpBnf = this;
        for (final Variable variable : variables) {
            tmpBnf = tmpBnf.addVar(variable);
        }
        return tmpBnf;
    }

    public BNF addVar(final Variable variable) {
        if (contains(variable)) {
            return this;
        }
        return new BNF(terminals,
                       Stream.concat(Stream.of(variable), variables.stream()).collect(toImmutableSet()),
                       start,
                       rules);
    }

    public BNF addTerminal(final Terminal terminal) {
        if (contains(terminal)) {
            return this;
        }
        return new BNF(Stream.concat(Stream.of(terminal), terminals.stream()).collect(toImmutableSet()),
                       variables,
                       start,
                       rules);
    }

    public boolean contains(final Symbol symbol) {
        return switch (symbol) {
            case Terminal $ -> terminals.stream().anyMatch(t -> t.name().equals(symbol.name()));
            case Variable $ -> variables.stream().anyMatch(v -> v.name().equals(symbol.name()));
        };
    }

    public BNF transformRule(final Variable variable, final Function<? super Rule, Rule> fn) {
        return addRule(fn.apply(getRuleFor(variable)));
    }

    public BNF transformRules(final Function<? super Rule, Rule> fn) {
        return updateRules(rules.stream().map(fn).collect(toCollection(LinkedHashSet::new)));
    }

    /**
     * Merges the grammar resulting from the given function into this grammar as if by {@code mergeRight(this, that)}.
     *
     * @param fn  function that builds a BNF using the fluent API
     */
    public BNF mergeFrom(final Function<FluentBNF.IBnfBody, FluentBNF.IBnfBody> fn) {
        final var bnf = fn.apply(FluentBNF.start(start)).build();
        return mergeRight(this, bnf);
    }

    /**
     * Merges 2 grammars preferring the starting variable and the rules from the right one.
     */
    public static BNF mergeRight(final BNF left, final BNF right) {
        final var newTerminals = concatSet(left.terminals(), right.terminals(), toImmutableSet());
        final var newVariables = concatSet(left.variables(), right.variables(), toImmutableSet());
        // right rules should come first so that distinct() prefers them over left rules
        final var newRules = StreamUtils.distinct(Stream.concat(right.rules.stream(), left.rules.stream()),
                                                  rule -> rule.lhs().name())
                .collect(toCollection(LinkedHashSet::new));
        return new BNF(newTerminals, newVariables, right.start(), newRules);
    }

    @Override
    public String toString() {
        return "<Σ=" + terminals + ", Γ=" + variables + ", ε=" + start + ", R=" + rules + ">";
    }

    private static <X> Set<X> concatSet(final Set<X> set1, final Set<X> set2, final Collector<X, ?, ? extends Set<X>> collector) {
        if (set1.isEmpty()) {
            return set2;
        } else if (set2.isEmpty()) {
            return set1;
        } else {
            return Stream.concat(set1.stream(), set2.stream()).collect(collector);
        }
    }

}
