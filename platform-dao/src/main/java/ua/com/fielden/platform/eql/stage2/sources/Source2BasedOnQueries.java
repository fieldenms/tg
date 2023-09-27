package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage3.operands.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;

public class Source2BasedOnQueries extends AbstractSource2 implements ISource2<Source3BasedOnQueries> {
    private final List<SourceQuery2> models = new ArrayList<>();
    public final boolean isSyntheticEntity;
    
    public Source2BasedOnQueries(final List<SourceQuery2> models, final String alias, final Integer id, final QuerySourceInfo<?> querySourceInfo, final boolean isSyntheticEntity, final boolean isExplicit, final boolean isPartOfCalcProp) {
        super(id, alias, querySourceInfo, isExplicit, isPartOfCalcProp);
        this.models.addAll(models);
        this.isSyntheticEntity = isSyntheticEntity;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return isSyntheticEntity ? Set.of(sourceType()) : models.stream().map(el -> el.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + models.hashCode();
        result = prime * result + (isSyntheticEntity ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof Source2BasedOnQueries)) {
            return false;
        }
        
        final Source2BasedOnQueries other = (Source2BasedOnQueries) obj;

        return Objects.equals(models, other.models) && Objects.equals(isSyntheticEntity, other.isSyntheticEntity);
    }

    @Override
    public TransformationResult2<Source3BasedOnQueries> transform(final TransformationContext2 context) {
        
        final List<SourceQuery3> transformedQueries = new ArrayList<>();
        TransformationContext2 currentContext = context.cloneWithNextSqlId();
        final int sqlId = currentContext.sqlId;
        
        for (final SourceQuery2 model : models) {
            final TransformationResult2<SourceQuery3> modelTr = model.transform(currentContext);
            transformedQueries.add(modelTr.item);
            currentContext = modelTr.updatedContext; // TODO should be just resolutionContext with propsResolutions added from this model transformation   
        }
           
        final Source3BasedOnQueries transformedSource = new Source3BasedOnQueries(transformedQueries, id, sqlId);
        return new TransformationResult2<>(transformedSource, currentContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final SourceQuery2 model : models) {
            result.addAll(model.collectProps());
        }
        return result;
    }
    
    @Override
    public String toString() {
        return sourceType().getSimpleName();
    }
}