package ua.com.fielden.platform.eql.stage1.sources;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.eql.stage1.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toSet;

public class Source1BasedOnQueries extends AbstractSource1<Source2BasedOnQueries> {

    public static final String ERR_YIELD_INTO_NON_EXISTENT_PROPERTY =
            "Cannot yield into non-existing property [%s] in entity type [%s], which is being used as a query source.";
    public static final String ERR_MODELS_OF_QUERY_SOURCE_HAVE_DIFFERENT_RESULT_TYPES =
            "Models of query source have different result types %s. " +
            "While making select(..) or join(..)/leftJoin(..) from multiple models it should be ensured that they are of the same result type.";

    private final List<SourceQuery1> models;
    private final boolean isSyntheticEntity;

    /**
     * @param alias  the alias of this source or {@code null}
     */
    public Source1BasedOnQueries(final List<SourceQuery1> models, final String alias, final Integer id, final Class<? extends AbstractEntity<?>> syntheticEntityType) {
        super(determineSourceType(models, syntheticEntityType), alias, id);
        this.isSyntheticEntity = syntheticEntityType != null;
        this.models = ImmutableList.copyOf(models);
    }

    private static Class<? extends AbstractEntity<?>> determineSourceType(final List<SourceQuery1> models, final Class<? extends AbstractEntity<?>> syntheticEntityType) {
        if (syntheticEntityType != null) {
            return syntheticEntityType;
        } else {
            final Set<Class<? extends AbstractEntity<?>>> modelsResultTypes = new HashSet<>();
            for (final SourceQuery1 model : models) {
                modelsResultTypes.add(model.resultType);
            }

            if (modelsResultTypes.size() != 1) {
                throw new EqlStage1ProcessingException(ERR_MODELS_OF_QUERY_SOURCE_HAVE_DIFFERENT_RESULT_TYPES.formatted(modelsResultTypes));
            }

            return modelsResultTypes.iterator().next();
        }
    }

    @Override
    public Source2BasedOnQueries transform(final TransformationContextFromStage1To2 context) {
        final List<SourceQuery2> transformedQueries = models.stream().map(m -> m.transform(context)).collect(toImmutableList());
        final QuerySourceInfo<?> ei = obtainQuerySourceInfo(context, transformedQueries, sourceType(), isSyntheticEntity);
        return new Source2BasedOnQueries(transformedQueries, alias, id, ei, isSyntheticEntity, true, context.isForCalcProp);
    }

    private static QuerySourceInfo<?> obtainQuerySourceInfo(
            final TransformationContextFromStage1To2 context,
            final List<SourceQuery2> models,
            final Class<? extends AbstractEntity<?>> sourceType,
            final boolean isSyntheticEntity)
    {
        if (isSyntheticEntity || (sourceType != EntityAggregates.class && allGenerated(models))) {
            return context.querySourceInfoProvider.getModelledQuerySourceInfo(sourceType);
        } else {
            return context.querySourceInfoProvider.produceQuerySourceInfoForEntityType(models, sourceType, false);
        }
    }

    private static boolean allGenerated(final List<SourceQuery2> models) {
        return models.stream().allMatch(model -> model.yields.allGenerated());
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return isSyntheticEntity ? Set.of(sourceType()) : models.stream().map(AbstractQuery1::collectEntityTypes).flatMap(Set::stream).collect(toSet());
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
        return this == obj ||
               obj instanceof Source1BasedOnQueries that
               && Objects.equals(models, that.models)
               && isSyntheticEntity == that.isSyntheticEntity
               && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("isSyntheticEntity", isSyntheticEntity)
                .add("models", models);
    }

}
