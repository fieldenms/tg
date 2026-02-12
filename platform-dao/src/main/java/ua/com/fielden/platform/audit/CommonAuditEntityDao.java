package ua.com.fielden.platform.audit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.eql.dbschema.PropertyInliner;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

/// Base type for implementations of audit-entity companion objects.
///
/// Cannot be used if auditing is disabled. Will throw [AuditingModeException] upon construction.
///
/// @param <E>  the audited entity type
///
public abstract class CommonAuditEntityDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractAuditEntity<E>>
        implements IEntityAuditor<E>
{

    private static final String
            ERR_AUDIT_PROPERTY_UNEXPECTED_NAME = "Audit-property [%s.%s] has unexpected name.",
            ERR_CREATING_INSERT_STMT = "Could not create an insert statement for [%s].";

    private static final String
            AUDIT_USER_COL = "AUDITUSER_",
            AUDIT_DATE_COL = "AUDITDATE_",
            AUDITED_ENTITY_COL = "AUDITEDENTITY_",
            AUDITED_VERSION_COL = "AUDITEDVERSION_",
            AUDITED_TRANSACTION_GUID_COL = "AUDITEDTRANSACTIONGUID_";

    // All fields below are effectively final, but cannot be declared so due to late initialisation.
    // TODO: use Stable Values (https://openjdk.org/jeps/502) once out of preview.
    private IAuditPropInstantiator<E> auditPropInstantiator;
    private IDomainMetadata domainMetadata;
    private PropertyInliner propInliner;
    private Class<E> auditedEntityType;
    private fetch<E> fetchModelForAuditing;

    /// Key: audited property. Value: audit-property.
    private Map<String, String> auditedToAuditPropertyNames;

    @Inject
    protected void init(
            final AuditingMode auditingMode,
            final IAuditTypeFinder a3tFinder,
            final IDomainMetadata domainMetadata,
            final PropertyInliner propInliner)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(this.getClass(), auditingMode);
        }

        this.domainMetadata = domainMetadata;
        this.propInliner = propInliner;
        auditedToAuditPropertyNames = makeAuditedToAuditPropertyNames(domainMetadata, getEntityType());
        final var propertiesForAuditing = collectPropertiesForAuditing(auditedToAuditPropertyNames.keySet());
        fetchModelForAuditing = makeFetchModelForAuditing(a3tFinder.navigateAudit(getEntityType()).auditedType(), propertiesForAuditing, domainMetadata);
        auditedEntityType = a3tFinder.navigateAudit(getEntityType()).auditedType();
        final var auditPropType = a3tFinder.navigateAudit(getEntityType()).auditPropType();
        auditPropInstantiator = co(auditPropType);
    }

    private static Set<String> collectPropertiesForAuditing(final Set<String> auditedProperties) {
        return ImmutableSet.<String>builder()
                .add(ID, VERSION)
                .addAll(auditedProperties)
                .build();
    }

    private static <E extends AbstractEntity<?>> fetch<E> makeFetchModelForAuditing(
            final Class<E> auditedType,
            final Set<String> propertiesForAuditing,
            final IDomainMetadata domainMetadata)
    {
        final var auditedTypeMetadata = domainMetadata.forEntity(auditedType);
        return foldLeft(propertiesForAuditing.stream(),
                        fetchNone(auditedType),
                        (fetch, prop) -> auditedTypeMetadata.property(prop).type().asEntity()
                                .map(et -> fetch.with(prop, fetchIdOnly(et.javaType())))
                                .orElseGet(() -> fetch.with(prop)));
    }

    @Override
    public fetch<E> fetchModelForAuditing() {
        return fetchModelForAuditing;
    }

    /// Returns the name of a property of this audit-entity type that audits the specified property of the audited entity type,
    /// if the specified property is indeed audited; otherwise, returns `null`.
    ///
    private @Nullable String getAuditPropertyName(final CharSequence auditedProperty) {
        return auditedToAuditPropertyNames.get(auditedProperty.toString());
    }

    @Override
    @SessionRequired
    public void audit(final Long auditedEntityId, final Long auditedEntityVersion, final String transactionGuid, final Collection<String> dirtyProperties) {
        final var dirtyAuditedPropertyNames = dirtyProperties.stream().filter(propName -> auditedPropertyNames().contains(propName)).collect(toSet());
        final var newEntityOrHasChangesToAudit = auditedEntityVersion == 0 || !dirtyAuditedPropertyNames.isEmpty();

        if (newEntityOrHasChangesToAudit) {
            final Long auditEntityId = insertAudit(auditedEntityId, auditedEntityVersion, transactionGuid);

            if (!dirtyAuditedPropertyNames.isEmpty()) {
                // Audit information about changed properties.
                final List<PropertyDescriptor<? extends AbstractSynAuditEntity<E>>> auditProps = dirtyAuditedPropertyNames.stream()
                        .map(this::getAuditPropertyName)
                        .map(propName -> auditPropInstantiator.pd(propName))
                        .collect(toImmutableList());
                if (!auditProps.isEmpty()) {
                    insertAuditProps(auditEntityId, auditProps);
                }
            }
        }
    }

    private static final String TEMPLATE_INSERT_FROM_SELECT_STMT = """
                INSERT INTO %1$s( -- Audit table.
                    %2$s,
                    %3$s,
                    %4$s,
                    %5$s,
                    %6$s,
                    %7$s,
                    %8$s,
                    %9$s
                )
                SELECT
                    ? AS %2$s,
                    0 AS %3$s,
                    %2$s AS %4$s,
                    %3$s AS %5$s,
                    ?  AS %6$s,
                    ?  AS %7$s,
                    ?  AS %8$s,
                    %10$s
                FROM %11$s -- Audited table.
                WHERE %2$s = ? AND %3$s = ?
                """;

    /// A cache for SQL insert statements for audit records.
    /// Keys are audit entity types.
    private static final Cache<Class<?>, String> CACHE_SQL_INSERT_AUDIT_RECORD_STMT = CacheBuilder.newBuilder().weakKeys().initialCapacity(10).maximumSize(100).concurrencyLevel(50).build();

    /// Returns an SQL insert statement for inserting an audit record.
    ///
    protected String sqlInsertAuditRecordStmt() {
        final var auditEntityType = getEntityType();
        try {
            return CACHE_SQL_INSERT_AUDIT_RECORD_STMT.get(auditEntityType, () -> {
                final var auditedToAuditPropNames = makeAuditedToAuditPropertyNames(domainMetadata, auditEntityType);

                final var mdAuditEntity = domainMetadata.forEntity(auditEntityType);
                final var tblAuditEntity = mdAuditEntity.asPersistent().map(md -> md.data().tableName()).orElseThrow();
                final var mdAuditedEntity = domainMetadata.forEntity(auditedEntityType);
                final var tblAuditedEntity = mdAuditedEntity.asPersistent().map(md -> md.data().tableName()).orElseThrow();

                final var auditColumnNames = mdAuditEntity.properties().stream()
                                             .filter(pm -> auditedToAuditPropNames.containsValue(pm.name()))
                                             .sorted()
                                             .flatMap(p -> p.asPersistent().stream())
                                             .flatMap(pm -> propInliner.inline(pm).orElseGet(() -> List.of(pm)).stream())
                                             .map(pm -> pm.data().column().name)
                                             .toList();
                final var auditedColumnNames = mdAuditedEntity.properties().stream()
                                               .filter(pm -> auditedToAuditPropNames.containsKey(pm.name()))
                                               .sorted()
                                               .flatMap(p -> p.asPersistent().stream())
                                               .flatMap(pm -> propInliner.inline(pm).orElseGet(() -> List.of(pm)).stream())
                                               .map(pm -> pm.data().column().name)
                                               .toList();

                final var auditColumnsForSql = String.join(",\n    ", auditColumnNames);
                final var auditedColumnsForSql = zip(auditedColumnNames, auditColumnNames, (col, a3t_col) -> col + " AS " + a3t_col)
                        .collect(joining(",\n    "));

                return TEMPLATE_INSERT_FROM_SELECT_STMT.formatted(
                        tblAuditEntity,
                        getDbVersion().idColumnName(),
                        getDbVersion().versionColumnName(),
                        AUDITED_ENTITY_COL, AUDITED_VERSION_COL, AUDIT_USER_COL, AUDIT_DATE_COL, AUDITED_TRANSACTION_GUID_COL,
                        auditColumnsForSql, auditedColumnsForSql, tblAuditedEntity);

            });
        } catch (final Exception ex) {
            throw new EntityCompanionException("Could not create an audit insert statement for entity [%s].".formatted(auditEntityType.getSimpleName()), ex);
        }
    }

    /// Inserts a record of the audit entity as a copy of the audited entity.
    ///
    /// @param auditedEntityId
    ///         the audited entity ID, used to identify a record to be audited
    /// @param auditedEntityVersion
    ///         the audited entity version, used to identify a record to be audited
    /// @param transactionGuid
    ///         identifier of a transaction that was used to save the audited entity
    /// @return an ID of the audit entity that was inserted
    ///
    private Long insertAudit(final Long auditedEntityId,  final Long auditedEntityVersion, final String transactionGuid) {
        final var insertStmt = sqlInsertAuditRecordStmt();
        final Long id = nextIdValue(ID_SEQUENCE_NAME, getSession());
        getSession().doWork(conn -> {
            try (final PreparedStatement ps = conn.prepareStatement(insertStmt)) {
                ps.setLong(1, id);
                ps.setLong(2, getUserOrThrow().getId());
                ps.setTimestamp(3, new java.sql.Timestamp(now().getMillis()));
                ps.setString(4, transactionGuid);
                ps.setLong(5, auditedEntityId);
                ps.setLong(6, auditedEntityVersion);
                ps.execute();
            }
        });

        return id;
    }

    private void insertAuditProps(final Long auditEntityId, final List<PropertyDescriptor<? extends AbstractSynAuditEntity<E>>> pdAuditProps) {
        final String stmt = auditPropInstantiator.sqlInsertAuditPropStmt();

        getSession().doWork(conn -> {
            try (final PreparedStatement ps = conn.prepareStatement(stmt)) {
                pdAuditProps.forEach(pd -> {
                    try {
                        ps.setLong(1, auditEntityId);
                        ps.setString(2, pd.toString());
                        ps.addBatch();
                    } catch (final SQLException ex) {
                        throw new DbException(ERR_CREATING_INSERT_STMT.formatted(auditPropInstantiator.auditPropEntityType().getSimpleName()), ex);
                    }
                });
                ps.executeBatch();
            }
        });
    }

    @Override
    protected IFetchProvider<AbstractAuditEntity<E>> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .filter(PropertyMetadata::isPersistent)
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

    private Collection<String> auditedPropertyNames() {
        return auditedToAuditPropertyNames.keySet();
    }

    /// Returns the current user, if defined.
    /// Otherwise, throws an exception.
    ///
    private User getUserOrThrow() {
        final var user = getUser();
        if (user == null) {
            throw new EntityCompanionException("The current user is not defined.");
        }
        return user;
    }

    public static Map<String, String> makeAuditedToAuditPropertyNames(final IDomainMetadata domainMetadata, final Class<? extends AbstractEntity<?>> entityType) {
        final var auditEntityMetadata = domainMetadata.forEntity(entityType);

        return auditEntityMetadata.properties()
                .stream()
                // Skip inactive audit properties.
                .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                .collect(toImmutableMap(p -> ofNullable(AuditUtils.auditedPropertyName(p.name())).orElseThrow(() -> new EntityDefinitionException(ERR_AUDIT_PROPERTY_UNEXPECTED_NAME.formatted(auditEntityMetadata.javaType().getSimpleName(), p.name()))),
                                        PropertyMetadata::name));
    }

}
