package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.SingleNodeSources2;

public class SingleNodeSources1 implements ISources1<SingleNodeSources2> {
    public final ISource1<?> source;

    public SingleNodeSources1(final ISource1<?> source) {
        this.source = source;
    }

    @Override
    public TransformationResult<SingleNodeSources2> transform(TransformationContext context) {
        final ISource2<?> mainTransformed = source.transform(context);
        return new TransformationResult<>(new SingleNodeSources2(mainTransformed), context.cloneWithAdded(mainTransformed));    
    }
    
    @Override
    public ISource1<? extends ISource2<?>> mainSource() {
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

        if (!(obj instanceof SingleNodeSources1)) {
            return false;
        }
        
        final SingleNodeSources1 other = (SingleNodeSources1) obj;
        
        return Objects.equals(source, other.source);
    }
}