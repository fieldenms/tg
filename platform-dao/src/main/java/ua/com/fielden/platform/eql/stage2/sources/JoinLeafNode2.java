package ua.com.fielden.platform.eql.stage2.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.IPropPathResolver;
import ua.com.fielden.platform.eql.stage2.IPropPathResolver.Resolution;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonPredicate3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.JoinInnerNode3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeafNode3;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

public record JoinLeafNode2 (ISource2<?> source) implements IJoinNode2<IJoinNode3>, ToString.IFormattable {

    @Override
    public Set<Prop2> collectProps() {
        return source.collectProps();
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return source.collectEntityTypes();
    }

    @Override
    public ISource2<? extends ISource3> mainSource() {
        return source;
    }

    @Override
    public TransformationResultFromStage2To3<IJoinNode3> transform(TransformationContextFromStage2To3 context) {
        final var explicitSourceTr = source.transform(context);
        return addImplicitJoins(explicitSourceTr.item, explicitSourceTr.updatedContext);
    }

    private TransformationResultFromStage2To3<IJoinNode3> addImplicitJoins(
            final ISource3 source,
            final TransformationContextFromStage2To3 context)
    {
        final var resJoins = context.propResolutions().joins().getOrDefault(source.id(), List.of());
        final var baseJoinNode = new JoinLeafNode3(source);
        final var baseContext = context.cloneWithSource(source);
        return foldLeft(resJoins, new TransformationResultFromStage2To3<>(baseJoinNode, baseContext), this::add);
    }

    private TransformationResultFromStage2To3<IJoinNode3> add(
            final TransformationResultFromStage2To3<IJoinNode3> acc,
            final IPropPathResolver.JoinNode resJoin)
    {
        final var accJoinNode = acc.item;
        final var context = acc.updatedContext;

        final var rightSource2 = resJoin.right();

        final var rightSource3Tr = rightSource2.transform(context);
        final var rightSource3 = rightSource3Tr.item;
        final var context2 = rightSource3Tr.updatedContext;

        final var rightJoinNodeTr = addImplicitJoins(rightSource3, context2);
        final var rightJoinNode = rightJoinNodeTr.item;
        final var context3 = rightJoinNodeTr.updatedContext;

        // The left ON operand is always typed as the referenced (right) entity.
        final var leftOnOperandTr = mkOperand(rightSource2.sourceType(), resJoin.leftOn(), context3);
        final var leftOnOperand = leftOnOperandTr.item;
        final var context4 = leftOnOperandTr.updatedContext;

        final var conds = new Conditions3(false, List.of(List.of(
                new ComparisonPredicate3(leftOnOperand, EQ, new Prop3(ID, rightSource3, LONG_PROP_TYPE))
        )));
        final var joinNode = new JoinInnerNode3(accJoinNode, rightJoinNode, resJoin.joinType(), conds);
        return new TransformationResultFromStage2To3<>(joinNode, context4);
    }

    private TransformationResultFromStage2To3<? extends ISingleOperand3> mkOperand(
            final Class<? extends AbstractEntity<?>> rightType,
            final Resolution resolution,
            final TransformationContextFromStage2To3 context)
    {
        return switch (resolution) {
            case Resolution.Column it -> new TransformationResultFromStage2To3<>(new Prop3(it.prop(), context.getSource(it.sourceId()), propType(rightType, H_ENTITY)), context);
            case Resolution.Expr it -> Expression3.simplify(it.expr().transform(context));
        };
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("source", source)
                .$();
    }

}
