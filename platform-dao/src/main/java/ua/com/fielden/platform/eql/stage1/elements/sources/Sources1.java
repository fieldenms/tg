package ua.com.fielden.platform.eql.stage1.elements.sources;

import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.types.tuples.T2;

public class Sources1  {
    public final IQrySource1<? extends IQrySource2<?>> main;
    private final List<CompoundSource1> compounds;

    public Sources1(final IQrySource1<? extends IQrySource2<?>> main, final List<CompoundSource1> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public T2<Sources2, PropsResolutionContext> transform(final PropsResolutionContext context) {
        final IQrySource2<?> mainTransformationResult = main.transform(context);    
        PropsResolutionContext currentContext = context.cloneWithAdded(mainTransformationResult);

        final List<CompoundSource2> transformed = new ArrayList<>();
        for (final CompoundSource1 compoundSource : compounds) {
            final T2<CompoundSource2, PropsResolutionContext> compoundSourceTransformationResult = compoundSource.transform(currentContext);
            transformed.add(compoundSourceTransformationResult._1);
            currentContext = compoundSourceTransformationResult._2;
        }
        return t2(new Sources2(mainTransformationResult, transformed), currentContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((compounds == null) ? 0 : compounds.hashCode());
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