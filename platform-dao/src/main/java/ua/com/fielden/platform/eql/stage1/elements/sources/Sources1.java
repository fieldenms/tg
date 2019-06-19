package ua.com.fielden.platform.eql.stage1.elements.sources;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class Sources1  {
    public final IQrySource1<? extends IQrySource2> main;
    private final List<CompoundSource1> compounds;

    public Sources1(final IQrySource1<? extends IQrySource2> main, final List<CompoundSource1> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public Sources1(final IQrySource1<? extends IQrySource2> main) {
        this(main, emptyList());
    }

    public TransformationResult<Sources2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends IQrySource2> mainTransformationResult = main.transform(resolutionContext);    
                
        final List<CompoundSource2> transformed = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = mainTransformationResult.getUpdatedContext();
        
        for (final CompoundSource1 compoundSource : compounds) {
            final TransformationResult<CompoundSource2> compoundSourceTransformationResult = compoundSource.transform(currentResolutionContext);
            transformed.add(compoundSourceTransformationResult.getItem());
            currentResolutionContext = compoundSourceTransformationResult.getUpdatedContext();
        }
        return new TransformationResult<Sources2>(new Sources2(mainTransformationResult.getItem(), transformed), currentResolutionContext);
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

        if (!(obj instanceof Sources1)) {
            return false;
        }
        
        final Sources1 other = (Sources1) obj;
        
        return Objects.equals(main, other.main) &&
                Objects.equals(compounds, other.compounds);
    }
}