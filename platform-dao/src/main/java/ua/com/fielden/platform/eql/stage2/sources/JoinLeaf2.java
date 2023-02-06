package ua.com.fielden.platform.eql.stage2.sources;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class JoinLeaf2 implements IJoinNode2<IJoinNode3> {
    public final ISource2<?> source;

    public JoinLeaf2(final ISource2<?> source) {
        this.source = source;
    }

    @Override
    public TransformationResult2<IJoinNode3> transform(TransformationContext2 context) {
        final TransformationResult2<IJoinNode3> sourceTransformed = IJoinNode2.transform(source, context);
        return new TransformationResult2<>(sourceTransformed.item, sourceTransformed.updatedContext);    
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>(); 
        result.addAll(source.collectProps());
        return result;
    }
    
    @Override
    public ISource2<? extends ISource3> mainSource() {
        return source;
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