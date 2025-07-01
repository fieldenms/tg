package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

final class DataValidatorUtils {

    public static String produceKeyUniquenessViolationSql(final Class<? extends AbstractEntity<?>> entityType, final List<CompiledRetriever> entityTypeRetrievers) {
        final List<String> keyProps = MigrationUtils.keyPaths(entityType);
        final var from = entityTypeRetrievers.stream().map(r -> RetrieverSqlProducer.getKeyResultsOnlySql(r.retriever, keyProps)).collect(joining("\nUNION ALL"));
        final var props = keyProps.stream().map(k -> " \"" + k + "\"").collect(joining(", "));
        return "SELECT 1 WHERE EXISTS (\nSELECT *, COUNT(*) FROM (" + from + ") T GROUP BY " + props + " HAVING COUNT(*) > 1\n)";
    }

    public static List<T3<String, String, String>> produceRequirednessValidationSql(final List<CompiledRetriever> retrieversJobs) {
        final var result = new ArrayList<T3<String, String, String>>();

        for (final CompiledRetriever retriever : retrieversJobs) {
            final var retrieverSql = RetrieverSqlProducer.getSqlWithoutOrdering(retriever.retriever);
            for (final PropInfo pi : retriever.getContainers()) {
                final var pmd = retriever.md.props().stream().filter(p -> p.name().equals(pi.propName())).findFirst().get();
                if (pmd.required()) {
                    final List<String> leafProps = isPersistentEntityType(pi.propType()) ? pmd.leafProps() : CollectionUtil.listOf(pmd.name());
                    final var cond = leafProps.stream().map(s -> "R. \"" + s + "\" IS NULL").collect(Collectors.joining(" AND "));
                    final var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond;
                    result.add(T3.t3(retriever.retriever.getClass().getSimpleName(), pi.propName() + ":" + pi.propType().getSimpleName(), sql));
                }
            }
        }

        return result;
    }

    public static List<T3<String, String, String>> produceDataIntegrityValidationSql(final List<CompiledRetriever> retrieversJobs) {
        final var result = new ArrayList<T3<String, String, String>>();
        final var entityTypeRetrievers = new HashMap<Class<? extends AbstractEntity<?>>, List<CompiledRetriever>>();

        for (final CompiledRetriever retriever : retrieversJobs) {
            final var retrieverSql = RetrieverSqlProducer.getSqlWithoutOrdering(retriever.retriever);
            for (final PropInfo pi : retriever.getContainers()) {
                if (isPersistentEntityType(pi.propType())) {
                    final List<String> keyProps = MigrationUtils.keyPaths((Class<? extends AbstractEntity>) pi.propType());
                    final List<String> leafProps = retriever.md.props().stream().filter(p -> p.name().equals(pi.propName())).findFirst().get().leafProps();
                    final var domainRets = entityTypeRetrievers.get(pi.propType());
                    final var from = domainRets == null ? null : domainRets.stream().map(r -> RetrieverSqlProducer.getKeyResultsOnlySql(r.retriever, keyProps)).collect(joining("\nUNION ALL"));
                    final var cond = "(" + leafProps.stream().map(s -> "R. \"" + s + "\" IS NOT NULL").collect(Collectors.joining(" OR ")) + ")";
                    final var existCond = " AND NOT EXISTS (SELECT * FROM (" + from + ") D WHERE " +
                            composeCondition(leafProps, keyProps, "R", "D") + ")";
                    final var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond + (from == null ? "" : existCond);
                    result.add(T3.t3(retriever.retriever.getClass().getSimpleName(), pi.propName() + ":" + pi.propType().getSimpleName(), sql));
                }
            }

            if (!retriever.retriever.isUpdater()) {
                final var existingOrCreated = entityTypeRetrievers.computeIfAbsent(retriever.retriever.type(), k -> new ArrayList<>());
                existingOrCreated.add(retriever);
            }
        }

        return result;
    }

    public static List<T3<String, String, String>> produceUpdatersKeysDataIntegrityValidationSql(final Map<CompiledRetriever, List<CompiledRetriever>> domainTypeRetrieversByUpdaters) {
        final var result = new ArrayList<T3<String, String, String>>();

        for (final Entry<CompiledRetriever, List<CompiledRetriever>> entry : domainTypeRetrieversByUpdaters.entrySet()) {
            final var retrieverSql = RetrieverSqlProducer.getSqlWithoutOrdering(entry.getKey().retriever);
            final List<String> keyProps = MigrationUtils.keyPaths((Class<? extends AbstractEntity>) entry.getKey().getType());
            final var domainRets = entry.getValue();
            final var from = domainRets == null ? null : domainRets.stream().map(r -> RetrieverSqlProducer.getKeyResultsOnlySql(r.retriever, keyProps)).collect(joining("\nUNION ALL"));
            final var cond = "(" + keyProps.stream().map(s -> "R. \"" + s + "\" IS NOT NULL").collect(Collectors.joining(" OR ")) + ")";
            final var existCond = " AND NOT EXISTS (SELECT * FROM (" + from + ") D WHERE " +
                    composeCondition(keyProps, keyProps, "R", "D") + ")";
            final var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond + (from == null ? "" : existCond);
            result.add(T3.t3(entry.getKey().retriever.getClass().getSimpleName(), "key", sql));
        }

        return result;
    }

    private static String composeCondition(final List<String> props, final List<String> keyProps, final String retAlias, final String domainAlias) {
        final List<T2<String, String>> pairs = new ArrayList<>();

        for (int i = 0; i < props.size(); i++) {
            pairs.add(T2.t2(props.get(i), keyProps.get(i)));
        }

        return pairs.stream().map(e -> "(" + retAlias + ".\"" + e._1 + "\" IS NULL AND " + domainAlias + ".\"" + e._2 + "\" IS NULL OR " + retAlias + ".\"" + e._1 + "\" = " + domainAlias + ".\"" + e._2 + "\")").collect(Collectors.joining(" AND "));
    }

}
