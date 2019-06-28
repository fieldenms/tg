package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.sources.CompoundSource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.Sources3;

public class Sources2 {
    public final IQrySource2<? extends IQrySource3> main;
    private final List<CompoundSource2> compounds;

    public Sources2(final IQrySource2<? extends IQrySource3> main, final List<CompoundSource2> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public Sources2(final IQrySource2<?> main) {
        this(main, emptyList());
    }

    public TransformationResult<Sources3> transform(final TransformationContext transformationContext) {
        final TransformationResult<? extends IQrySource3> mainTransformationResult = main.transform(transformationContext);    
        
        final List<CompoundSource3> transformed = new ArrayList<>();
        TransformationContext currentResolutionContext = mainTransformationResult.updatedContext;
        
        for (final CompoundSource2 compoundSource : compounds) {
            final TransformationResult<CompoundSource3> compoundSourceTransformationResult = compoundSource.transform(currentResolutionContext);
            
            transformed.add(compoundSourceTransformationResult.item);
            currentResolutionContext = compoundSourceTransformationResult.updatedContext;
        }
        return new TransformationResult<Sources3>(new Sources3(mainTransformationResult.item, transformed), currentResolutionContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((compounds == null) ? 0 : compounds.hashCode());
        result = prime * result + ((main == null) ? 0 : main.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sources2)) {
            return false;
        }

        final Sources2 other = (Sources2) obj;

        return Objects.equals(main, other.main) && Objects.equals(compounds, other.compounds);
    }
}