package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractQuery2 implements ToString.IFormattable {

    public final Optional<IJoinNode2<? extends IJoinNode3>> maybeJoinRoot;
    public final Conditions2 whereConditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;
    public final Class<?> resultType;

    public AbstractQuery2(final QueryComponents2 queryComponents, final Class<?> resultType) {
        this(queryComponents.maybeJoinRoot(),
             queryComponents.whereConditions(),
             queryComponents.yields(),
             queryComponents.groups(),
             queryComponents.orderings(),
             resultType);
    }

    public AbstractQuery2(
            final Optional<IJoinNode2<? extends IJoinNode3>> maybeJoinRoot,
            final Conditions2 whereConditions,
            final Yields2 yields,
            final GroupBys2 groups,
            final OrderBys2 orderings,
            final Class<?> resultType)
    {
        this.maybeJoinRoot = maybeJoinRoot;
        this.whereConditions = whereConditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
        this.resultType = resultType;
    }

    /**
     * Returns a new query of this type where orderings are replaced by the specified ones.
     */
    public abstract AbstractQuery2 setOrderings(final OrderBys2 orderings);

    /**
     * Transforms all query components to stage 3.
     *
     * @param context
     * @return
     */
    protected TransformationResultFromStage2To3<QueryComponents3> transformQueryComponents(final TransformationContextFromStage2To3 context) {
        final AbstractQuery2 this_ = switch (context.dbVersion()) {
            case MSSQL ->  UnionOrderById.INSTANCE.apply(this);
            default -> this;
        };

        return this_.transformQueryComponents_(context);
    }

    private TransformationResultFromStage2To3<QueryComponents3> transformQueryComponents_(final TransformationContextFromStage2To3 context) {
        final var joinRootTr = maybeJoinRoot.map(joinRoot -> joinRoot.transform(context))
                .orElseGet(() -> new TransformationResultFromStage2To3<>(null, context));
        final TransformationResultFromStage2To3<Conditions3> whereConditionsTr = whereConditions.transform(joinRootTr.updatedContext);
        final TransformationResultFromStage2To3<Yields3> yieldsTr = yields.transform(whereConditionsTr.updatedContext, this);
        final TransformationResultFromStage2To3<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResultFromStage2To3<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        return new TransformationResultFromStage2To3<>(new QueryComponents3(Optional.ofNullable(joinRootTr.item), whereConditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item), orderingsTr.updatedContext);
    }

    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        maybeJoinRoot.map(ITransformableFromStage2To3::collectProps).ifPresent(result::addAll);
        result.addAll(whereConditions.collectProps());
        result.addAll(yields.collectProps());
        result.addAll(groups.collectProps());
        result.addAll(orderings.collectProps());

        return result;
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        maybeJoinRoot.map(ITransformableFromStage2To3::collectEntityTypes).ifPresent(result::addAll);
        result.addAll(whereConditions.collectEntityTypes());
        result.addAll(yields.collectEntityTypes());
        result.addAll(groups.collectEntityTypes());
        result.addAll(orderings.collectEntityTypes());

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + whereConditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + maybeJoinRoot.hashCode();
        result = prime * result + yields.hashCode();
        result = prime * result + orderings.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractQuery2 that
                  && Objects.equals(resultType, that.resultType)
                  && Objects.equals(maybeJoinRoot, that.maybeJoinRoot)
                  && Objects.equals(yields, that.yields)
                  && Objects.equals(whereConditions, that.whereConditions)
                  && Objects.equals(groups, that.groups)
                  && Objects.equals(orderings, that.orderings);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("resultType", resultType)
                .addIfNot("whereConditions", whereConditions, Conditions2::isEmpty)
                .addIfPresent("join", maybeJoinRoot)
                .addIfNot("where", whereConditions, Conditions2::isEmpty)
                .addIfNot("yields", yields, Yields2::isEmpty)
                .addIfNot("groups", groups, GroupBys2::isEmpty)
                .addIfNot("orderings", orderings, OrderBys2::isEmpty)
                .pipe(this::addToString)
                .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}
