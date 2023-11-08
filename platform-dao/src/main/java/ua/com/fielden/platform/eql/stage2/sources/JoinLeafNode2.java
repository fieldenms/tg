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
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
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
    public TransformationResult2<IJoinNode3> transform(TransformationContext2 context) {
        final TransformationResult2<? extends ISource3> explicitSourceTr = source.transform(context);
        return generateJoinNode(explicitSourceTr.item, context.getSourceImplicitNodes(source.id()), explicitSourceTr.updatedContext);
    }

    /**
     * Depending on the existence of implicit nodes either {@code JoinLeafNode3} or {@code JoinInnerNode3} is generated.
     *
     * @param source
     * @param implicitNodes
     * @param context
     * @return
     */
    private static TransformationResult2<IJoinNode3> generateJoinNode(final ISource3 source, final List<ImplicitNode> implicitNodes, final TransformationContext2 context) {
        // registering source within transformation context
        TransformationContext2 currentContext = context.cloneWithSource(source);
        IJoinNode3 currentJoinNode = new JoinLeafNode3(source);

        // enhancing current join node with implicit nodes
        for (final ImplicitNode implicitNode : implicitNodes) {
            final TransformationResult2<JoinInnerNode3> tr = joinImplicitNode(currentJoinNode, implicitNode, source, currentContext);
            currentJoinNode = tr.item;
            currentContext = tr.updatedContext;
        }

        return new TransformationResult2<>(currentJoinNode, currentContext);
    }

    /**
     *
     * @param currentJoinNode -- join node that will become left node in the inner node being generated as a result of the join
     * @param implicitNode -- implicit node, which serves as a base for generation of the right node in the inner node being generated as a result of the join
     * @param rootSource
     * @param context
     * @return
     */
    private static TransformationResult2<JoinInnerNode3> joinImplicitNode(final IJoinNode3 currentJoinNode, final ImplicitNode implicitNode, final ISource3 rootSource, final TransformationContext2 context) {
        final TransformationResult2<? extends ISource3> tr = implicitNode.source.transform(context);
        final ISource3 addedSource = tr.item;
        TransformationContext2 currentContext = tr.updatedContext;

        final ISingleOperand3 leftOperand;

        if (implicitNode.expr == null) {
            leftOperand = new Prop3(implicitNode.name, rootSource, new PropType(implicitNode.source.sourceType(), H_ENTITY));
        } else {
            final TransformationResult2<Expression3> expTr = implicitNode.expr.transform(currentContext);
            leftOperand = expTr.item.isSingle() ? expTr.item.first : expTr.item;
            currentContext = expTr.updatedContext;
        }

        final Prop3 rightOperand = new Prop3(ID, addedSource, LONG_PROP_TYPE);
        final ComparisonPredicate3 comparisonPredicate = new ComparisonPredicate3(leftOperand, EQ, rightOperand);
        final Conditions3 joinOnConditions = new Conditions3(false, asList(asList(comparisonPredicate)));
        final TransformationResult2<IJoinNode3> implicitJoinNodeTr = generateJoinNode(addedSource, implicitNode.subnodes(), currentContext);
        return new TransformationResult2<>(new JoinInnerNode3(currentJoinNode, implicitJoinNodeTr.item, (implicitNode.nonnullable ? IJ : LJ), joinOnConditions), implicitJoinNodeTr.updatedContext);
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