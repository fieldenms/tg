package ua.com.fielden.platform.eql.stage2.sources;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.AbstractQuery2;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class Source2BasedOnQueries extends AbstractSource2 implements ISource2<Source3BasedOnQueries> {
    private final List<SourceQuery2> models;
    public final boolean isSyntheticEntity;

    public Source2BasedOnQueries(final List<SourceQuery2> models, final String alias, final Integer id, final QuerySourceInfo<?> querySourceInfo, final boolean isSyntheticEntity, final boolean isExplicit, final boolean isPartOfCalcProp) {
        super(id, alias, querySourceInfo, isExplicit, isPartOfCalcProp);
        this.models = ImmutableList.copyOf(models);
        this.isSyntheticEntity = isSyntheticEntity;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return isSyntheticEntity
                ? Set.of(sourceType())
                : models.stream().map(AbstractQuery2::collectEntityTypes).flatMap(Set::stream).collect(toSet());
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
        return this == obj
               || obj instanceof Source2BasedOnQueries that
                  && Objects.equals(models, that.models)
                  && isSyntheticEntity == that.isSyntheticEntity
                  && super.equals(that);
    }

    @Override
    public TransformationResultFromStage2To3<Source3BasedOnQueries> transform(final TransformationContextFromStage2To3 context) {

        final List<SourceQuery3> transformedQueries = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;

        for (final SourceQuery2 model : models) {
            final TransformationResultFromStage2To3<SourceQuery3> modelTr = model.transform(currentContext);
            transformedQueries.add(modelTr.item);
            currentContext = modelTr.updatedContext;
        }

        currentContext = currentContext.cloneWithNextSqlId();

        final Source3BasedOnQueries transformedSource = new Source3BasedOnQueries(transformedQueries, id, currentContext.sqlId);
        return new TransformationResultFromStage2To3<>(transformedSource, currentContext);
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
