package ua.com.fielden.platform.eql.stage1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.Sources2;
import ua.com.fielden.platform.utils.Pair;

public class Sources1  {
    private final IQrySource1<? extends IQrySource2> main;
    private final List<CompoundSource1> compounds;

    public Sources1(final IQrySource1<? extends IQrySource2> main, final List<CompoundSource1> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public Pair<Sources2, PropsResolutionContext> transform(final PropsResolutionContext resolutionContext) {
        final Pair<? extends IQrySource2, PropsResolutionContext> mainTransformationResult = main.transform(resolutionContext);    
                
        final List<CompoundSource2> transformed = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = mainTransformationResult.getValue();
        
        for (final CompoundSource1 compoundSource : compounds) {
            Pair<CompoundSource2, PropsResolutionContext> compoundSourceTransformationResult = compoundSource.transform(currentResolutionContext);
            transformed.add(compoundSourceTransformationResult.getKey());
            currentResolutionContext = compoundSourceTransformationResult.getValue();
        }
        return new Pair<>(new Sources2(mainTransformationResult.getKey(), transformed), currentResolutionContext);
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

    public IQrySource1<? extends IQrySource2> getMain() {
        return main;
    }

    public List<CompoundSource1> getCompounds() {
        return compounds;
    }

    public List<IQrySource1<? extends IQrySource2>> getAllSources() {
        final List<IQrySource1<? extends IQrySource2>> result = new ArrayList<>();
        result.add(main);
        for (final CompoundSource1 compound : compounds) {
            result.add(compound.getSource());
        }
        return result;
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