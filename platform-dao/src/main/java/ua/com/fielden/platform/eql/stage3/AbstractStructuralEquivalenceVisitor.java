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
/// This visitor is abstract in the state parameter, enabling operations built on top of it to manage their own state.
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
    protected Boolean defaultValue(final INode3 x, final INode3 y, final S state) {
        throw new InvalidStateException("No default");
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Operands

    @Override
    public Boolean prop(final Prop3 x, final Prop3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && x.name.equals(y.name)
               && x.source.id().equals(y.source.id());
    }

    @Override
    public Boolean value(final Value3 x, final Value3 y, final S state) {
        // Parameter name is irrelevant, only the actual value matters.
        // Type is important too: 123 as Integer is distinct from 123 as String.
        return Objects.equals(x.type(), y.type())
               && Objects.equals(x.value(), y.value());
    }

    @Override
    public Boolean expression(final Expression3 x, final Expression3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.expression(x, y, state);
    }

    @Override
    public Boolean compoundSingleOperand(final CompoundSingleOperand3 x, final CompoundSingleOperand3 y, final S state) {
        return x.operator().equals(y.operator())
                && super.compoundSingleOperand(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Single-operand functions -- all compare the operand and the type.

    @Override
    public Boolean absOf(final AbsOf3 x, final AbsOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.absOf(x, y, state);
    }

    @Override
    public Boolean ceil(final Ceil3 x, final Ceil3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.ceil(x, y, state);
    }

    @Override
    public Boolean floor(final Floor3 x, final Floor3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.floor(x, y, state);
    }

    @Override
    public Boolean dateOf(final DateOf3 x, final DateOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.dateOf(x, y, state);
    }

    @Override
    public Boolean dayOf(final DayOf3 x, final DayOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.dayOf(x, y, state);
    }

    @Override
    public Boolean dayOfWeekOf(final DayOfWeekOf3 x, final DayOfWeekOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.dayOfWeekOf(x, y, state);
    }

    @Override
    public Boolean monthOf(final MonthOf3 x, final MonthOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.monthOf(x, y, state);
    }

    @Override
    public Boolean yearOf(final YearOf3 x, final YearOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.yearOf(x, y, state);
    }

    @Override
    public Boolean hourOf(final HourOf3 x, final HourOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.hourOf(x, y, state);
    }

    @Override
    public Boolean minuteOf(final MinuteOf3 x, final MinuteOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.minuteOf(x, y, state);
    }

    @Override
    public Boolean secondOf(final SecondOf3 x, final SecondOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.secondOf(x, y, state);
    }

    @Override
    public Boolean lowerCaseOf(final LowerCaseOf3 x, final LowerCaseOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.lowerCaseOf(x, y, state);
    }

    @Override
    public Boolean upperCaseOf(final UpperCaseOf3 x, final UpperCaseOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.upperCaseOf(x, y, state);
    }

    @Override
    public Boolean maxOf(final MaxOf3 x, final MaxOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.maxOf(x, y, state);
    }

    @Override
    public Boolean minOf(final MinOf3 x, final MinOf3 y, final S state) {
        return Objects.equals(x.type, y.type) && super.minOf(x, y, state);
    }

    @Override
    public Boolean sumOf(final SumOf3 x, final SumOf3 y, final S state) {
        return x.distinct == y.distinct
               && Objects.equals(x.type, y.type)
               && super.sumOf(x, y, state);
    }

    @Override
    public Boolean countOf(final CountOf3 x, final CountOf3 y, final S state) {
        return x.distinct == y.distinct
               && Objects.equals(x.type, y.type)
               && super.countOf(x, y, state);
    }

    @Override
    public Boolean averageOf(final AverageOf3 x, final AverageOf3 y, final S state) {
        return x.distinct == y.distinct
               && Objects.equals(x.type, y.type)
               && super.averageOf(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Two-operand functions -- all compare both operands and the type.

    @Override
    public Boolean ifNull(final IfNull3 x, final IfNull3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.ifNull(x, y, state);
    }

    @Override
    public Boolean roundTo(final RoundTo3 x, final RoundTo3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.roundTo(x, y, state);
    }

    @Override
    public Boolean addDateInterval(final AddDateInterval3 x, final AddDateInterval3 y, final S state) {
        return Objects.equals(x.intervalUnit, y.intervalUnit)
               && Objects.equals(x.type, y.type)
               && super.addDateInterval(x, y, state);
    }

    @Override
    public Boolean countDateInterval(final CountDateInterval3 x, final CountDateInterval3 y, final S state) {
        return Objects.equals(x.intervalUnit, y.intervalUnit)
               && Objects.equals(x.type, y.type)
               && super.countDateInterval(x, y, state);
    }

    @Override
    public Boolean concatOf(final ConcatOf3 x, final ConcatOf3 y, final S state) {
        return Objects.equals(x.type, y.type)
               && super.concatOf(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Other functions

    @Override
    public Boolean concat(final Concat3 x, final Concat3 y, final S state) {
        return Objects.equals(x.type(), y.type())
               && super.concat(x, y, state);
    }

    @Override
    public Boolean caseWhen(final CaseWhen3 x, final CaseWhen3 y, final S state) {
        return x.typeCast().equals(y.typeCast())
               && Objects.equals(x.type, y.type)
               && super.caseWhen(x, y, state);
    }

    @Override
    public Boolean countAll(final CountAll3 x, final CountAll3 y, final S state) {
        // CountAll3 is a singleton and has no arguments; its inherited equals compares only the (fixed) type.
        return Objects.equals(x.type, y.type);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    @Override
    public Boolean subQuery(final SubQuery3 x, final SubQuery3 y, final S state) {
        return Objects.equals(x.type(), y.type()) && super.subQuery(x, y, state);
    }

    @Override
    protected Boolean visitQueryComponents(final AbstractQuery3 x, final AbstractQuery3 y, final S state) {
        return Objects.equals(x.resultType, y.resultType)
               && super.visitQueryComponents(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Conditions

    @Override
    public Boolean conditions(final Conditions3 x, final Conditions3 y, final S state) {
        return x.negated() == y.negated()
               && super.conditions(x, y, state);
    }

    @Override
    public Boolean comparisonPredicate(final ComparisonPredicate3 x, final ComparisonPredicate3 y, final S state) {
        return Objects.equals(x.operator(), y.operator())
               && super.comparisonPredicate(x, y, state);
    }

    @Override
    public Boolean nullPredicate(final NullPredicate3 x, final NullPredicate3 y, final S state) {
        return x.negated() == y.negated()
               && super.nullPredicate(x, y, state);
    }

    @Override
    public Boolean likePredicate(final LikePredicate3 x, final LikePredicate3 y, final S state) {
        return Objects.equals(x.options(), y.options())
               && super.likePredicate(x, y, state);
    }

    @Override
    public Boolean setPredicate(final SetPredicate3 x, final SetPredicate3 y, final S state) {
        return x.negated() == y.negated()
               && super.setPredicate(x, y, state);
    }

    @Override
    public Boolean existencePredicate(final ExistencePredicate3 x, final ExistencePredicate3 y, final S state) {
        return x.negated() == y.negated()
               && super.existencePredicate(x, y, state);
    }

    @Override
    public Boolean quantifiedPredicate(final QuantifiedPredicate3 x, final QuantifiedPredicate3 y, final S state) {
        return Objects.equals(x.operator(), y.operator())
               && Objects.equals(x.quantifier(), y.quantifier())
               && super.quantifiedPredicate(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Join nodes

    @Override
    public Boolean joinInnerNode(final JoinInnerNode3 x, final JoinInnerNode3 y, final S state) {
        return Objects.equals(x.joinType(), y.joinType())
               && super.joinInnerNode(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Set operands

    @Override
    public Boolean operandsBasedSet(final OperandsBasedSet3 x, final OperandsBasedSet3 y, final S state) {
        return visitAll(x.operands(), y.operands(), state);
    }

    @Override
    public Boolean queryBasedSet(final QueryBasedSet3 x, final QueryBasedSet3 y, final S state) {
        return visit(x.model(), y.model(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sources.
    // : `columns` is derived from the table/models, so it is not compared.

    @Override
    public Boolean sourceBasedOnTable(final Source3BasedOnTable x, final Source3BasedOnTable y, final S state) {
        return Objects.equals(x.sqlAlias, y.sqlAlias)
               && Objects.equals(x.id(), y.id())
               && Objects.equals(x.tableName, y.tableName);
    }

    @Override
    public Boolean sourceBasedOnQueries(final Source3BasedOnQueries x, final Source3BasedOnQueries y, final S state) {
        return Objects.equals(x.sqlAlias, y.sqlAlias)
               && Objects.equals(x.id(), y.id())
               && visitAll(x.models, y.models, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sundries

    /// [Yield3#column()] is excluded as it used only for SQL generation.
    ///
    @Override
    public Boolean yield(final Yield3 x, final Yield3 y, final S state) {
        return Objects.equals(x.alias(), y.alias())
               && Objects.equals(x.type(), y.type())
               && super.yield(x, y, state);
    }

    @Override
    public Boolean orderBy(final OrderBy3 x, final OrderBy3 y, final S state) {
        return x.isDesc() == y.isDesc() && super.orderBy(x, y, state);
    }

    @Override
    protected Boolean yields(final Yields3 xs, final Yields3 ys, final S state) {
        final var xsMap = xs.yieldsMap();
        final var ysMap = ys.yieldsMap();
        if (!xsMap.keySet().equals(ysMap.keySet())) {
            return false;
        }
        return super.yields(xs, ys, state);
    }

    @Override
    protected Boolean orderBys(final OrderBys3 xs, final OrderBys3 ys, final S state) {
        return Objects.equals(xs.limit(), ys.limit())
               && xs.offset() == ys.offset()
               && visitAll(xs.list(), ys.list(), state);
    }

}
