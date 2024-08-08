package fielden.platform.bnf;

import java.util.*;

import static fielden.platform.bnf.Sequence.seqOrTerm;

/**
 * Fluent API that can be used to define a {@link BNF} grammar.
 * <p>
 * To use the API, begin with {@link #start(Variable)}, then add arbitrary rules, and finally end with {@link IBnfBody#build()}.
 */
public final class FluentBNF {

    private FluentBNF() {}

    public static IBnfBody start(Variable start) {
        return new BnfBodyImpl(start);
    }

    public interface IBnfBody {
        IDerivation derive(Variable v);
        ISpecialization specialize(Variable v);
        BNF build();
    }

    public interface IDerivation {
        IDerivationTail to(final Term... terms);
    }

    public interface IDerivationTail extends IBnfBody {
        IDerivationTail or(final Term... terms);
    }

    public interface ISpecialization {
        IBnfBody into(final Variable... vs);
    }

    private static class BnfBodyImpl implements FluentBNF.IBnfBody {

        protected final Builder builder;

        BnfBodyImpl(final Variable start) {
            this.builder = new Builder(start);
        }

        BnfBodyImpl(final Builder builder) {
            this.builder = builder;
        }

        @Override
        public IDerivation derive(final Variable v) {
            if (buildsRule())
                builder.add(finishRule());
            return new DerivationImpl(builder, v);
        }

        @Override
        public ISpecialization specialize(final Variable v) {
            if (buildsRule())
                builder.add(finishRule());
            return new SpecializationImpl(builder, v);
        }

        @Override
        public BNF build() {
            builder.add(finishRule());
            return new BNF(builder.terminals, builder.variables, builder.start, builder.rules);
        }

        /**
         * If this fluent interface {@linkplain #buildsRule() builds a rule}, then builds a rule and returns it, otherwise fails.
         */
        protected Rule finishRule() {
            throw new UnsupportedOperationException();
        }

        /**
         * Does this fluent interface build a rule? If so, then {@link #finishRule()} should be used during parsing.
         */
        protected boolean buildsRule() {
            return false;
        }

    }

    private static final class DerivationImpl extends BnfBodyImpl implements FluentBNF.IDerivation, FluentBNF.IDerivationTail {
        private final Variable lhs;
        private final List<Term> bodies = new ArrayList<>();

        DerivationImpl(final Builder builder, final Variable lhs) {
            super(builder);
            this.lhs = lhs;
        }

        /**
         * Specify a body for this derivation rule. Multiple bodies may be specified with {@link IDerivationTail#or(Term...)},
         * which will result into an {@linkplain Alternation alternation} with bodies as choices.
         */
        @Override
        public IDerivationTail to(final Term... terms) {
            bodies.add(seqOrTerm(terms));
            return this;
        }

        /**
         * Specify a body for this derivation rule. Multiple bodies may be specified, which will result into an
         * {@linkplain Alternation alternation} with bodies as choices.
         */
        @Override
        public IDerivationTail or(final Term... terms) {
            bodies.add(seqOrTerm(terms));
            return this;
        }

        @Override
        protected Rule finishRule() {
            return new Derivation(lhs, new Alternation(bodies));
        }

        @Override
        protected boolean buildsRule() {
            return true;
        }
    }

    private static final class SpecializationImpl extends BnfBodyImpl implements FluentBNF.ISpecialization {
        private final Variable lhs;
        private final List<Variable> rhs = new ArrayList<>();

        SpecializationImpl(final Builder builder, final Variable lhs) {
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
            return new Specialization(lhs, rhs);
        }

        @Override
        protected boolean buildsRule() {
            return true;
        }
    }

    private static final class Builder {
        final Set<Terminal> terminals = new LinkedHashSet<>();
        final Set<Variable> variables = new LinkedHashSet<>();
        final Set<Rule> rules = new LinkedHashSet<>();
        final Variable start;

        Builder(Variable start) {
            this.start = start;
            add(start);
        }

        void add(final Rule rule) {
            add(rule.lhs());
            rule.rhs().options().forEach(this::add);
            rules.add(rule);
        }

        void add(final Notation notation) {
            switch (notation) {
                case Alternation alternation -> alternation.options().forEach(this::add);
                case Quantifier quantifier -> add(quantifier.term());
            }
        }

        void add(final Sequence sequence) {
            sequence.forEach(this::add);
        }

        void add(final Variable v) {
            variables.add(v);
        }

        void add(final Terminal terminal) {
            terminals.add(terminal);
        }

        void add(final Symbol symbol) {
            switch (symbol) {
                case Terminal x -> add(x);
                case Variable x -> add(x);
            }
        }

        void add(final Term term) {
            switch (term) {
                case Notation x -> add(x);
                case Sequence x -> add(x);
                case Symbol x -> add(x);
            }
        }
    }

}
