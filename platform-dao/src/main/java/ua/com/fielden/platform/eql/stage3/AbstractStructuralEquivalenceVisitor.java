package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.eql.stage3.conditions.*;
import ua.com.fielden.platform.eql.stage3.operands.*;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.AbstractQuery3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.JoinInnerNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;

import java.util.Objects;
import java.util.stream.Stream;

/// Ordinary structural equivalence of two stage-3 AST trees: two nodes are equivalent iff they have the same type and
/// their corresponding fields are equivalent -- child nodes recursively, everything else by value.
/// Generated identifiers are compared as-is (this is *not* alpha-equivalence).
///
/// Each `visit` reproduces the `equals` of the corresponding node, but every reference to a child node is compared
/// through [AbstractSameShapeVisitor#visit(Object, Object, Object)] rather than through `Object.equals`; only
/// genuine leaves
/// (types, names, flags, parameters, enums, `Class`) are compared by value with [Objects#equals].
///
/// Two fields are deliberately excluded, matching the current `equals`: [Yield3]'s derived `column`, and a source's
/// `columns` (derived from its table or models).
///
abstract class AbstractStructuralEquivalenceVisitor<S> extends AbstractSameShapeVisitor<Boolean, S> {

    @Override
    protected Boolean noMatch(final Object x, final Object y, final S state) {
        return false;
    }

    /// Structural equivalence combines the results for a node's children with logical *and*.
    ///
    @Override
    protected Boolean combine(final Boolean a, final Boolean b) {
        return a && b;
    }

    @Override
    protected Boolean combine(final Stream<Boolean> stream) {
        return stream.allMatch(b -> b);
    }

    @Override
    protected final Boolean identity() {
        return true;
    }

    /// This visitor covers all nodes, so there is no default.
    ///
    @Override
    protected Boolean defaultValue(final Object x, final Object y, final S state) {
        throw new InvalidStateException("No default");
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Operands

    @Override
    public Boolean visit(final Prop3 x, final Prop3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && x.name.equals(y.name)
               && x.source.id().equals(y.source.id());
    }

    @Override
    public Boolean visit(final Value3 x, final Value3 y, final S state) {
        // Parameter name is irrelevant, only the actual value matters.
        // Type is important too: 123 as Integer is distinct from 123 as String.
        return Objects.equals(x.type(), y.type())
               && Objects.equals(x.value(), y.value());
    }

    @Override
    public Boolean visit(final Expression3 x, final Expression3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final CompoundSingleOperand3 x, final CompoundSingleOperand3 y, final S state) {
        return x.operator().equals(y.operator())
                && super.visit(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Single-operand functions -- all compare the operand and the type.

    @Override
    public Boolean visit(final AbsOf3 x, final AbsOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final Ceil3 x, final Ceil3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final Floor3 x, final Floor3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final DateOf3 x, final DateOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final DayOf3 x, final DayOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final DayOfWeekOf3 x, final DayOfWeekOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final MonthOf3 x, final MonthOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final YearOf3 x, final YearOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final HourOf3 x, final HourOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final MinuteOf3 x, final MinuteOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final SecondOf3 x, final SecondOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final LowerCaseOf3 x, final LowerCaseOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final UpperCaseOf3 x, final UpperCaseOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final MaxOf3 x, final MaxOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final MinOf3 x, final MinOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final SumOf3 x, final SumOf3 y, final S state) {
        return x.distinct == y.distinct
               && Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final CountOf3 x, final CountOf3 y, final S state) {
        return x.distinct == y.distinct
               && Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final AverageOf3 x, final AverageOf3 y, final S state) {
        return x.distinct == y.distinct
               && Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Two-operand functions -- all compare both operands and the type.

    @Override
    public Boolean visit(final IfNull3 x, final IfNull3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final RoundTo3 x, final RoundTo3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final AddDateInterval3 x, final AddDateInterval3 y, final S state) {
        return Objects.equals(x.intervalUnit, y.intervalUnit)
               && Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final CountDateInterval3 x, final CountDateInterval3 y, final S state) {
        return Objects.equals(x.intervalUnit, y.intervalUnit)
               && Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final ConcatOf3 x, final ConcatOf3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Other functions

    @Override
    public Boolean visit(final Concat3 x, final Concat3 y, final S state) {
        return Objects.equals(x.type(), y.type())
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final CaseWhen3 x, final CaseWhen3 y, final S state) {
        return x.typeCast().equals(y.typeCast())
               && Objects.equals(x.type, y.type)
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final CountAll3 x, final CountAll3 y, final S state) {
        // CountAll3 is a singleton and has no arguments; its inherited equals compares only the (fixed) type.
        return Objects.equals(x.type, y.type);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    @Override
    public Boolean visit(final SubQuery3 x, final SubQuery3 y, final S state) {
        return Objects.equals(x.type(), y.type()) && super.visit(x, y, state);
    }

    @Override
    protected Boolean visitQueryComponents(final AbstractQuery3 x, final AbstractQuery3 y, final S state) {
        return Objects.equals(x.resultType, y.resultType)
               && super.visitQueryComponents(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Conditions

    @Override
    public Boolean visit(final Conditions3 x, final Conditions3 y, final S state) {
        return x.negated() == y.negated()
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final ComparisonPredicate3 x, final ComparisonPredicate3 y, final S state) {
        return Objects.equals(x.operator(), y.operator())
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final NullPredicate3 x, final NullPredicate3 y, final S state) {
        return x.negated() == y.negated()
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final LikePredicate3 x, final LikePredicate3 y, final S state) {
        return Objects.equals(x.options(), y.options())
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final SetPredicate3 x, final SetPredicate3 y, final S state) {
        return x.negated() == y.negated()
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final ExistencePredicate3 x, final ExistencePredicate3 y, final S state) {
        return x.negated() == y.negated()
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final QuantifiedPredicate3 x, final QuantifiedPredicate3 y, final S state) {
        return Objects.equals(x.operator(), y.operator())
               && Objects.equals(x.quantifier(), y.quantifier())
               && super.visit(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Join nodes

    @Override
    public Boolean visit(final JoinInnerNode3 x, final JoinInnerNode3 y, final S state) {
        return Objects.equals(x.joinType(), y.joinType())
               && super.visit(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Set operands

    @Override
    public Boolean visit(final OperandsBasedSet3 x, final OperandsBasedSet3 y, final S state) {
        return visitAll(x.operands(), y.operands(), state);
    }

    @Override
    public Boolean visit(final QueryBasedSet3 x, final QueryBasedSet3 y, final S state) {
        return visit(x.model(), y.model(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sources.
    // : `columns` is derived from the table/models, so it is not compared.

    @Override
    public Boolean visit(final Source3BasedOnTable x, final Source3BasedOnTable y, final S state) {
        return Objects.equals(x.sqlAlias, y.sqlAlias)
               && Objects.equals(x.id(), y.id())
               && Objects.equals(x.tableName, y.tableName);
    }

    @Override
    public Boolean visit(final Source3BasedOnQueries x, final Source3BasedOnQueries y, final S state) {
        return Objects.equals(x.sqlAlias, y.sqlAlias)
               && Objects.equals(x.id(), y.id())
               && visitAll(x.models, y.models, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sundries

    /// [Yield3#column()] is excluded as it used only for SQL generation.
    ///
    @Override
    public Boolean visit(final Yield3 x, final Yield3 y, final S state) {
        return Objects.equals(x.alias(), y.alias())
               && Objects.equals(x.type(), y.type())
               && super.visit(x, y, state);
    }

    @Override
    public Boolean visit(final OrderBy3 x, final OrderBy3 y, final S state) {
        return x.isDesc() == y.isDesc() && super.visit(x, y, state);
    }

    protected Boolean visit(final Yields3 xs, final Yields3 ys, final S state) {
        final var xsMap = xs.yieldsMap();
        final var ysMap = ys.yieldsMap();
        if (!xsMap.keySet().equals(ysMap.keySet())) {
            return false;
        }
        return super.visit(xs, ys, state);
    }

    protected Boolean visit(final OrderBys3 xs, final OrderBys3 ys, final S state) {
        return Objects.equals(xs.limit(), ys.limit())
               && xs.offset() == ys.offset()
               && visitAll(xs.list(), ys.list(), state);
    }

}
