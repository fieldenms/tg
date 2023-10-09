package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.TransformationResult1;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.JoinLeafNode2;

public class JoinLeafNode1 implements IJoinNode1<JoinLeafNode2> {
    public final ISource1<?> source;

    public JoinLeafNode1(final ISource1<?> source) {
        this.source = source;
    }

    @Override
    public TransformationResult1<JoinLeafNode2> transform(TransformationContext1 context) {
        final ISource2<?> mainTransformed = source.transform(context);
        return new TransformationResult1<>(new JoinLeafNode2(mainTransformed), context.cloneWithAdded(mainTransformed));
    }
    
    @Override
    public ISource1<? extends ISource2<?>> mainSource() {
        return source;
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return source.collectEntityTypes();
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

        if (!(obj instanceof JoinLeafNode1)) {
            return false;
        }
        
        final JoinLeafNode1 other = (JoinLeafNode1) obj;
        
        return Objects.equals(source, other.source);
    }
}