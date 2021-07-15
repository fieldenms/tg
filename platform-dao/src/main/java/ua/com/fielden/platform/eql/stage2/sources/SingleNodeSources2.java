package ua.com.fielden.platform.eql.stage2.sources;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public class SingleNodeSources2 implements ISources2<ISources3> {
    public final ISource2<?> source;

    public SingleNodeSources2(final ISource2<?> source) {
        this.source = source;
    }

    @Override
    public TransformationResult<ISources3> transform(TransformationContext context) {
        final TransformationResult<ISources3> sourceTransformed = ISources2.transform(source, context);
        return new TransformationResult<>(sourceTransformed.item, sourceTransformed.updatedContext);    
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

        if (!(obj instanceof SingleNodeSources2)) {
            return false;
        }
        
        final SingleNodeSources2 other = (SingleNodeSources2) obj;
        
        return Objects.equals(source, other.source);
    }
}