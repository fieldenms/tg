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
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.JoinBranch3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeaf3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;

public class JoinLeaf2 implements IJoinNode2<IJoinNode3> {
    public final ISource2<?> source;

    public JoinLeaf2(final ISource2<?> source) {
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

    private static TransformationResult2<IJoinNode3> generateJoinNode(final ISource3 source, final List<ImplicitNode> implicitNodes, final TransformationContext2 context) {
        TransformationContext2 currentContext = context.cloneWithSource(source);
        IJoinNode3 currentJoinNode = new JoinLeaf3(source);

        for (final ImplicitNode implicitNode : implicitNodes) {
            final TransformationResult2<IJoinNode3> tr = joinImplicitNode(currentJoinNode, implicitNode, source, currentContext);
            currentJoinNode = tr.item;
            currentContext = tr.updatedContext;
        }

        return new TransformationResult2<>(currentJoinNode, currentContext);
    }
    
    private static TransformationResult2<IJoinNode3> joinImplicitNode(final IJoinNode3 currentJoinNode, final ImplicitNode implicitNode, final ISource3 rootSource, final TransformationContext2 context) {
        final TransformationResult2<Source3BasedOnTable> tr = implicitNode.source.transform(context);
        final Source3BasedOnTable addedSource = tr.item;
        TransformationContext2 currentContext = tr.updatedContext; 

        final ISingleOperand3 lo;
        
        if (implicitNode.expr == null) {
            lo = new Prop3(implicitNode.name, rootSource, new PropType(implicitNode.source.sourceType(), H_ENTITY));
        } else {
            final TransformationResult2<Expression3> expTr = implicitNode.expr.transform(currentContext);
            lo = expTr.item.isSingle() ? expTr.item.first : expTr.item;
            currentContext = expTr.updatedContext;
        }
        
        final Prop3 ro = new Prop3(ID, addedSource, LONG_PROP_TYPE);
        final ComparisonTest3 ct = new ComparisonTest3(lo, EQ, ro);
        final Conditions3 jc = new Conditions3(false, asList(asList(ct)));
        final TransformationResult2<IJoinNode3> implicitJoinNodeTr = generateJoinNode(addedSource, implicitNode.subnodes(), currentContext);
        return new TransformationResult2<>(new JoinBranch3(currentJoinNode, implicitJoinNodeTr.item, (implicitNode.required ? IJ : LJ), jc), implicitJoinNodeTr.updatedContext);
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

        if (!(obj instanceof JoinLeaf2)) {
            return false;
        }
        
        final JoinLeaf2 other = (JoinLeaf2) obj;
        
        return Objects.equals(source, other.source);
    }
}