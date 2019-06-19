package ua.com.fielden.platform.eql.stage1.elements.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this(main, Collections.emptyList());
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
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(main);
        for (final CompoundSource1 compound : compounds) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public List<CompoundSource1> getCompounds() {
        return compounds;
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Sources1)) {
            return false;
        }
        final Sources1 other = (Sources1) obj;
        if (compounds == null) {
            if (other.compounds != null) {
                return false;
            }
        } else if (!compounds.equals(other.compounds)) {
            return false;
        }
        if (main == null) {
            if (other.main != null) {
                return false;
            }
        } else if (!main.equals(other.main)) {
            return false;
        }
        return true;
    }
}