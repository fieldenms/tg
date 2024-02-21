package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.internal.grammar.rules.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;

public record BNF(
        Set<Token> tokens,
        Set<Variable> variables,
        Variable start,
        Set<Rule> rules
) {

    public BNF(final Set<Token> tokens, final Set<Variable> variables, final Variable start, final Set<Rule> rules) {
        this.tokens = unmodifiableSet(new LinkedHashSet<>(tokens));
        this.variables = unmodifiableSet(new LinkedHashSet<>(variables));
        this.start = start;
        this.rules = unmodifiableSet(new LinkedHashSet<>(rules));
    }

    /** @return stream of all grammar symbols */
    public Stream<Symbol> symbols() {
        return Stream.concat(tokens.stream(), variables.stream());
    }

    @Override
    public String toString() {
        return "<Σ=" + tokens + ", Γ=" + variables + ", ε=" + start + ", R=" + rules + ">";
    }

    public sealed interface Rule permits Derivation, Specialization {

        Variable lhs();

        Stream<Body> rhs();

        /**
         * Returns all variables present in the right-hand side of this rule.
         */
        default Stream<Variable> variables() {
            return Stream.concat(variables(components()), variables(quantifiedSymbols()));
        }

        /**
         * Returns all tokens present in the right-hand side of this rule.
         */
        default Stream<Token> tokens() {
            return Stream.concat(tokens(components()), tokens(quantifiedSymbols()));
        }

        private Stream<Component> components() {
            return rhs().flatMap(Collection::stream);
        }

        private Stream<Component> quantifiedSymbols() {
            return quantifiers().flatMap(Quantifier::symbols);
        }

        private Stream<Quantifier> quantifiers() {
            return components().filter(Component::isQuantifier).map(Component::asQuantifier);
        }

        private static Stream<Token> tokens(final Stream<Component> components) {
            return components.filter(Component::isToken).map(Component::asToken);
        }

        private static Stream<Variable> variables(final Stream<Component> components) {
            return components.filter(Component::isVariable).map(Component::asVariable);
        }
    }

    public final static class Derivation implements Rule {
        private final Variable lhs;
        private final List<Body> rhs;

        public Derivation(Variable lhs, List<Body> rhs) {
            this.lhs = lhs;
            this.rhs = List.copyOf(rhs);
        }

        @Override
        public Stream<Body> rhs() {
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
                    rhs.stream().map(body -> body.stream().map(Component::toString).collect(joining(" ")))
                            .collect(joining(" | ")));
        }
    }

    public record Specialization(Variable lhs, List<Variable> specializers) implements Rule {
        @Override
        public Stream<Body> rhs() {
            return specializers.stream().map(Body::new);
        }

        @Override
        public String toString() {
            return "%s -> %s".formatted(lhs, specializers.stream().map(Variable::name).collect(joining(" | ")));
        }
    }

    public interface Fluent {

        static IBnfBody start(Variable start) {
            return new FluentImpl(start);
        }

        interface IBnfBody {
            IDerivation derive(Variable v);
            ISpecialization specialize(Variable v);
            BNF build();
        }

        interface IDerivation {
            IDerivationTail to(final TempComponent... cs);
        }

        interface IDerivationTail extends IBnfBody {
            IDerivationTail or(final TempComponent... cs);
        }

        interface ISpecialization {
            IBnfBody into(final Variable... vs);
        }

    }

    private static class FluentImpl implements Fluent, Fluent.IBnfBody {
        protected final Builder builder;

        FluentImpl(final Variable start) {
            this.builder = new Builder(start);
        }

        FluentImpl(final Builder builder) {
            this.builder = builder;
        }

        @Override
        public IDerivation derive(final Variable v) {
            if (buildsRule())
                builder.add(finishRule());
            return new Derivation(builder, v);
        }

        @Override
        public ISpecialization specialize(final Variable v) {
            if (buildsRule())
                builder.add(finishRule());
            return new Specialization(builder, v);
        }

        @Override
        public BNF build() {
            builder.add(finishRule());
            return new BNF(builder.tokens, builder.variables, builder.start, builder.rules);
        }

        protected Rule finishRule() { throw new UnsupportedOperationException(); }

        protected boolean buildsRule() { return false; }

        private static class Derivation extends FluentImpl implements IDerivation, IDerivationTail {
            private final Variable lhs;
            private final List<Body> bodies = new ArrayList<>();

            Derivation(final Builder builder, final Variable lhs) {
                super(builder);
                this.lhs = lhs;
            }

            @Override
            public IDerivationTail to(final TempComponent... cs) {
                bodies.add(new Body(FluentImpl.normalize(cs).toList()));
                return this;
            }

            @Override
            public IDerivationTail or(final TempComponent... cs) {
                bodies.add(new Body(FluentImpl.normalize(cs).toList()));
                return this;
            }

            @Override
            protected Rule finishRule() {
                return new BNF.Derivation(lhs, bodies);
            }

            @Override
            protected boolean buildsRule() {
                return true;
            }
        }

        private static class Specialization extends FluentImpl implements ISpecialization {
            private final Variable lhs;
            private final List<Variable> rhs = new ArrayList<>();

            Specialization(final Builder builder, final Variable lhs) {
                super(builder);
                this.lhs = lhs;
            }

            @Override
            public IBnfBody into(final Variable... vs) {
                Collections.addAll(rhs, vs);
                return this;
            }

            @Override
            protected Rule finishRule() {
                return new BNF.Specialization(lhs, rhs);
            }

            @Override
            protected boolean buildsRule() {
                return true;
            }
        }

        private static Stream<Component> normalize(final TempComponent... cs) {
            return Arrays.stream(cs).map(TempComponent::normalize);
        }

    }

    private static class Builder {
        private final Set<Token> tokens = new LinkedHashSet<>();
        private final Set<Variable> variables = new LinkedHashSet<>();
        private final Set<Rule> rules = new LinkedHashSet<>();
        private final Variable start;

        private Builder(Variable start) {
            this.start = start;
            add(start);
        }

        private void add(final Rule rule) {
            add(rule.lhs());
            rule.rhs().forEach(this::add);
            rules.add(rule);
        }

        private void add(Body body) {
            body.forEach(this::add);
        }

        private void add(final Quantifier q) {
            q.symbols().forEach(this::add);
        }

        private void add(final Variable v) {
            variables.add(v);
        }

        private void add(final Token t) {
            tokens.add(t);
        }

        private void add(final Component component) {
            switch (component) {
                case Quantifier q -> add(q);
                case Token tok -> add(tok);
                case Variable v -> add(v);
                default -> throw new IllegalStateException("Unexpected value: " + component);
            }
        }

    }

}
