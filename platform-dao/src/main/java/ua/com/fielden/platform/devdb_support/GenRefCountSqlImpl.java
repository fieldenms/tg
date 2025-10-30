package ua.com.fielden.platform.devdb_support;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies.DomainEntityDependency;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.entityDependencyMap;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

@Singleton
class GenRefCountSqlImpl implements IGenRefCountSql {

    private final IApplicationDomainProvider appDomainProvider;
    private final EqlTables eqlTables;
    private final EqlQueryTransformer eqlQueryTransformer;
    private final IDomainMetadata domainMetadata;
    private final DbVersion dbVersion;

    @Inject
    GenRefCountSqlImpl(
            final IApplicationDomainProvider appDomainProvider,
            final EqlTables eqlTables,
            final EqlQueryTransformer eqlQueryTransformer,
            final IDomainMetadata domainMetadata,
            final IDbVersionProvider dbVersionProvider)
    {
        this.appDomainProvider = appDomainProvider;
        this.eqlTables = eqlTables;
        this.eqlQueryTransformer = eqlQueryTransformer;
        this.domainMetadata = domainMetadata;
        this.dbVersion = dbVersionProvider.dbVersion();
    }

    @Override
    public List<String> generateStatements(
            final Collection<Class<? extends AbstractEntity<?>>> entityTypes,
            final DependencyPredicate dependencyPredicate)
    {
        final List<String> sqls = new ArrayList<>();

        if (dbVersion == MSSQL) {
            sqls.add("SET QUOTED_IDENTIFIER ON");
        }

        final var entityDependencyMap = entityDependencyMap(entityTypes);

        entityTypes.stream()
                .filter(EntityUtils::isActivatablePersistentEntityType)
                .sorted(comparing(Class::getSimpleName))
                .forEach(entityType -> {
                    sqls.add("UPDATE %s SET %s = 0".formatted(tableName(entityType), columnName(entityType, REF_COUNT)));
                    final var deps = entityDependencyMap.get(entityType).getActivatableDependencies().stream()
                            .filter(dep -> isPersistentEntityType(dep.entityType()))
                            .filter(dep -> dependencyPredicate.test(entityType, dep.entityType(), dep.propPath()))
                            .toList();
                    if (!deps.isEmpty()) {
                        // We are combining plain SQL with EQL here, and we need to refer, from EQL, to a column that appears in plain SQL.
                        // That column corresponds to `id` of the entity being updated.
                        // Hence, we use a placeholder in EQL that is later replaced by the desired column name.
                        final int idPlaceholder = 777999777;
                        final var refCountSql = deps.stream()
                                .sorted(ddComparator)
                                .map(dep -> {
                                    final var query = select(dep.entityType())
                                            .where()
                                            .prop(ACTIVE).eq().val(true)
                                            .and()
                                            .prop(dep.propPath()).eq().val(idPlaceholder)
                                            .yield().countAll().as("result")
                                            .modelAsAggregate();
                                    return compileToSql(query);
                                })
                                .map("(%s)"::formatted)
                                .map(sql -> sql.replace(String.valueOf(idPlaceholder), "%s.%s".formatted(tableName(entityType), columnName(entityType, ID))))
                                .collect(joining("\n+\n"));

                        final var stmt = "UPDATE %s SET %s = %n(%s)%n WHERE %s = 'Y'".formatted(
                                tableName(entityType),
                                columnName(entityType, REF_COUNT),
                                refCountSql,
                                columnName(entityType, ACTIVE));
                        sqls.add(stmt);
                    }
                });

        return sqls;
    }

    @Override
    public List<String> generateStatements(final Collection<Class<? extends AbstractEntity<?>>> entityTypes) {
        return generateStatements(entityTypes, DependencyPredicate.constantly(true));
    }

    @Override
    public List<String> generateStatements(final DependencyPredicate dependencyPredicate) {
        return generateStatements(appDomainProvider.entityTypes(), dependencyPredicate);
    }

    @Override
    public List<String> generateStatements() {
        return generateStatements(appDomainProvider.entityTypes());
    }

    private String compileToSql(final QueryModel<?> query) {
        final var model = new QueryProcessingModel<>(query, null, null, Map.of(), true);
        return eqlQueryTransformer.getModelResult(model, Optional.empty()).sql();
    }

    private String tableName(final Class<? extends AbstractEntity<?>> entityType) {
        final var table = eqlTables.getTableForEntityType(entityType);
        if (table == null) {
            throw new InvalidArgumentException("There is no table for entity type [%s].".formatted(entityType.getSimpleName()));
        }
        return table.name();
    }

    private String columnName(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        final var pm = domainMetadata.forProperty(entityType, property)
                .asPersistent()
                .orElseThrow(() -> new InvalidArgumentException(format(
                        "Cannot obtain column name for non-persistent property [%s.%s].",
                        entityType.getSimpleName(), property)));
        return pm.data().column().name;
    }

    private static final Comparator<? super DomainEntityDependency> ddComparator =
            comparing((DomainEntityDependency d) -> d.entityType().getSimpleName())
                    .thenComparing(DomainEntityDependency::propPath);

}
