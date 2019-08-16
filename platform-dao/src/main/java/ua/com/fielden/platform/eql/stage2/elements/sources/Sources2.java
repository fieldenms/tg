package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.elements.sources.JoinedQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.SingleQrySource3;

public class Sources2 {
    public final IQrySource2<? extends IQrySource3> main;
    private final List<CompoundSource2> compounds;

    public Sources2(final IQrySource2<? extends IQrySource3> main, final List<CompoundSource2> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public TransformationResult<IQrySources3> transform(final TransformationContext context) {
        final TransformationResult<? extends IQrySource3> mainTransformationResult = main.transform(context);    
        TransformationContext currentContext = mainTransformationResult.updatedContext;
        IQrySources3 currentResult = new SingleQrySource3(mainTransformationResult.item);
        
        for (final CompoundSource2 compoundSource : compounds) {
            final TransformationResult<IQrySource3> compSrcTransformationResult = compoundSource.source.transform(currentContext);
            final TransformationResult<Conditions3> compConditionsTransformationResult = compoundSource.joinConditions.transform(compSrcTransformationResult.updatedContext);
            currentResult = new JoinedQrySource3(currentResult, new SingleQrySource3(compSrcTransformationResult.item), compoundSource.joinType, compConditionsTransformationResult.item);
            currentContext = compConditionsTransformationResult.updatedContext;
        }
        
        return new TransformationResult<IQrySources3>(currentResult, currentContext);
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