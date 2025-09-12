package ua.com.fielden.platform.eql.stage3.sources;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.utils.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.eql.meta.PropType.NULL_TYPE;
import static ua.com.fielden.platform.utils.StreamUtils.enumerate;
import static ua.com.fielden.platform.utils.StreamUtils.transpose;

/**
 * A query source formed by concatenating results of its underlying queries.
 */
public class Source3BasedOnQueries extends AbstractSource3 {

    private static final String ERR_SOURCE_QUERIES_NUMBER_OF_YIELDS = "Source queries that form a union must have the same number of yields.";

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<SourceQuery3> models;
    
    public Source3BasedOnQueries(final List<SourceQuery3> models, final Integer id, final int sqlId) {
        // It is sufficient to use just the first model's yields because all models in a list must share the same yield aliases.
        // See YieldInfoNodesGenerator.
        super("Q_" + sqlId, id, obtainColumnsFromYields(validateModels(models).getFirst().yields.getYields()));
        this.models = ImmutableList.copyOf(models);
    }

    private static List<SourceQuery3> validateModels(final List<SourceQuery3> models) {
        // number of yields is valid only if either there are no yields or all models have the same number of yields
        final Boolean isValidNumberOfYields = StreamUtils.areAllEqual(models.stream().mapToInt(m -> m.yields.size())).orElse(true);
        if (!isValidNumberOfYields) {
            final var exception = new EqlStage3ProcessingException(ERR_SOURCE_QUERIES_NUMBER_OF_YIELDS);
            LOGGER.error(() -> "%s\nQueries:%n%s".formatted(exception.getMessage(),
                                                            enumerate(models.stream(), 1, (m, i) -> "%s. %s".formatted(i, m)).collect(joining("\n"))));
            throw exception;
        }

        return models;
    }
    
    private static Map<String, String> obtainColumnsFromYields(final Collection<Yield3> yields) {
        return yields.stream().collect(toMap(Yield3::alias, Yield3::column));
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        if (dbVersion == POSTGRESQL) {
            // 1. Issue #2213 - PostgreSQL requires explicit type casts
            // 2. a SELECT with an ORDER BY inside a UNION must be enclosed in parentheses, although this inner ordering
            // is not guaranteed to have an effect on the results of UNION
            // https://www.postgresql.org/docs/16/sql-select.html#SQL-UNION
            final List<PropType> types = expectedYieldTypes();
            return "("
                  + models.stream().map(m -> '(' + m.sql(metadata, dbVersion, types) + ')').collect(joining("\n UNION ALL \n"))
                  + ") AS " + sqlAlias;
        } else {
            return "("
            + models.stream().map(m -> m.sql(metadata, dbVersion)).collect(joining("\n UNION ALL \n"))
            + ") AS " + sqlAlias;
        }
    }

    /**
     * Returns a list of types corresponding to yielded values that will form columns of the result set.
     * <p>
     * It is assumed that the types of yielded values are compatible across underlying queries.
     * It is also assumed that the underlying queries are equal in the number of yielded values.
     */
    private List<PropType> expectedYieldTypes() {
        // in each column of the result set find the first value with non-NULL type; assume that all values in the same
        // column are compatible with each other in terms of their types
        return transpose(models, q -> q.yields.getYields().stream())
                .map(column -> column.stream().map(Yield3::type).filter(PropType::isNotNull).findFirst().orElse(NULL_TYPE))
                .toList();
    }

    @Override
    public String toString() {
        return "Source3BasedOnQueries of type " + models.getFirst().resultType;
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
        return this == obj
               || obj instanceof Source3BasedOnQueries that
                  && Objects.equals(this.models, that.models)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("models", models);
    }

}
