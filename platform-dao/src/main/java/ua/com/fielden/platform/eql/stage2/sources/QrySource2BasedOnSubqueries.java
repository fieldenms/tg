package ua.com.fielden.platform.eql.stage2.sources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage3.operands.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sources.QrySource3BasedOnSubqueries;

public class QrySource2BasedOnSubqueries extends AbstractQrySource2 implements IQrySource2<QrySource3BasedOnSubqueries> {
    private final List<SourceQuery2> models = new ArrayList<>();

    public QrySource2BasedOnSubqueries(final List<SourceQuery2> models, final String alias, final String id, final EntityInfo<?> entityInfo) {
        super(id, alias, entityInfo);
        this.models.addAll(models);
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return (Class<? extends AbstractEntity<?>>) models.get(0).resultType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + models.hashCode();
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

        if (!(obj instanceof QrySource2BasedOnSubqueries)) {
            return false;
        }
        
        final QrySource2BasedOnSubqueries other = (QrySource2BasedOnSubqueries) obj;

        return Objects.equals(models, other.models);
    }

    @Override
    public TransformationResult<QrySource3BasedOnSubqueries> transform(final TransformationContext context) {
        
        final List<SourceQuery3> transformedQueries = new ArrayList<>();
        TransformationContext currentResolutionContext = context.cloneWithNextSqlId();
        final int sqlId = currentResolutionContext.sqlId;
        
        for (final SourceQuery2 model : models) {
            final TransformationResult<SourceQuery3> modelTr = model.transform(currentResolutionContext);
            transformedQueries.add(modelTr.item);
            currentResolutionContext = modelTr.updatedContext; // TODO should be just resolutionContext with propsResolutions added from this model transformation   
        }
           
        final QrySource3BasedOnSubqueries transformedSource = new QrySource3BasedOnSubqueries(transformedQueries, id, sqlId);
        return new TransformationResult<QrySource3BasedOnSubqueries>(transformedSource, currentResolutionContext);
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