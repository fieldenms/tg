package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.H_ENTITY;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonPredicate3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.JoinInnerNode3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeafNode3;

public class JoinLeafNode2 implements IJoinNode2<IJoinNode3> {
    public final ISource2<?> source;

    public JoinLeafNode2(final ISource2<?> source) {
        this.source = source;
    }

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
        final TransformationResultFromStage2To3<? extends ISource3> explicitSourceTr = source.transform(context);
        return generateJoinNode(explicitSourceTr.item, context.getHelperNodesForSource(source.id()), explicitSourceTr.updatedContext);
    }

    /**
     * Depending on the existence of helper nodes either {@code JoinLeafNode3} or {@code JoinInnerNode3} is generated.
     *
     * @param source
     * @param helperNodes
     * @param context
     * @return
     */
    private static TransformationResultFromStage2To3<IJoinNode3> generateJoinNode(final ISource3 source, final List<HelperNodeForImplicitJoins> helperNodes, final TransformationContextFromStage2To3 context) {
        // registering source within transformation context
        TransformationContextFromStage2To3 currentContext = context.cloneWithSource(source);
        IJoinNode3 currentJoinNode = new JoinLeafNode3(source);

        // enhancing current join node with helper nodes
        for (final HelperNodeForImplicitJoins helperNode : helperNodes) {
            final TransformationResultFromStage2To3<JoinInnerNode3> tr = joinImplicitNode(currentJoinNode, helperNode, source, currentContext);
            currentJoinNode = tr.item;
            currentContext = tr.updatedContext;
        }

        return new TransformationResultFromStage2To3<>(currentJoinNode, currentContext);
    }

    /**
     *
     *
     * @param currentJoinNode -- join node that will become {@code leftNode} in a {@link JoinInnerNode3} instance being generated as a result of the join.
     * @param helperNode -- helper node, which serves as a base for generation of {@code rightNode} in a {@link JoinInnerNode3} instance being generated as a result of the implicit join.
     * @param rootSource
     * @param context
     * @return
     */
    private static TransformationResultFromStage2To3<JoinInnerNode3> joinImplicitNode(final IJoinNode3 currentJoinNode, final HelperNodeForImplicitJoins helperNode, final ISource3 rootSource, final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISource3> tr = helperNode.source.transform(context);
        final ISource3 addedSource = tr.item;
        TransformationContextFromStage2To3 currentContext = tr.updatedContext;

        final ISingleOperand3 leftOperand;

        if (helperNode.expr == null) {
            leftOperand = new Prop3(helperNode.name, rootSource, new PropType(helperNode.source.sourceType(), H_ENTITY));
        } else {
            final TransformationResultFromStage2To3<Expression3> exprTransRes = helperNode.expr.transform(currentContext);
            leftOperand = exprTransRes.item.isSingleOperandExpression() ? exprTransRes.item.firstOperand : exprTransRes.item;
            currentContext = exprTransRes.updatedContext;
        }

        final Prop3 rightOperand = new Prop3(ID, addedSource, LONG_PROP_TYPE);
        final ComparisonPredicate3 comparisonPredicate = new ComparisonPredicate3(leftOperand, EQ, rightOperand);
        final Conditions3 joinOnConditions = new Conditions3(false, asList(asList(comparisonPredicate)));
        final TransformationResultFromStage2To3<IJoinNode3> implicitJoinNodeTr = generateJoinNode(addedSource, helperNode.subnodes(), currentContext);
        return new TransformationResultFromStage2To3<>(new JoinInnerNode3(currentJoinNode, implicitJoinNodeTr.item, (helperNode.nonnullable ? IJ : LJ), joinOnConditions), implicitJoinNodeTr.updatedContext);
    }

    @Override
    public int hashCode() {
        return 31 + source.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JoinLeafNode2)) {
            return false;
        }

        final JoinLeafNode2 other = (JoinLeafNode2) obj;

        return Objects.equals(source, other.source);
    }
}