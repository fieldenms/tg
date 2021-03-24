package ua.com.fielden.platform.eql.stage1.sources;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage2.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.Sources2;

public class Sources1  {
    public final ISource1<? extends ISource2<?>> main;
    private final List<CompoundSource1> compounds;

    public Sources1(final ISource1<? extends ISource2<?>> main, final List<CompoundSource1> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public TransformationResult<Sources2> transform(final TransformationContext context) {
        final ISource2<?> mainTransformed = main.transform(context);    
        TransformationContext currentContext = context.cloneWithAdded(mainTransformed);

        final List<CompoundSource2> transformed = new ArrayList<>();
        for (final CompoundSource1 compoundSource : compounds) {
            final TransformationResult<CompoundSource2> compoundSourceTransformationResult = compoundSource.transform(currentContext);
            transformed.add(compoundSourceTransformationResult.item);
            currentContext = compoundSourceTransformationResult.updatedContext;
        }
        return new TransformationResult<>(new Sources2(mainTransformed, transformed), currentContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + compounds.hashCode();
        result = prime * result + main.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sources1)) {
            return false;
        }
        
        final Sources1 other = (Sources1) obj;
        
        return Objects.equals(main, other.main) &&
                Objects.equals(compounds, other.compounds);
    }
}