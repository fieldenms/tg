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
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.eql.dbschema.PropertyInliner;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.StreamUtils;

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
            ERR_CREATING_INSERT_STMT = "Could not create insert for [%s].";

    private static final int AUDIT_PROP_BATCH_SIZE = 100;

    private final String auditUserCol = "AUDITUSER_";
    private final String auditDateCol = "AUDITDATE_";
    private final String auditedEntityCol = "AUDITEDENTITY_";
    private final String auditedVersionCol = "AUDITEDVERSION_";
    private final String auditedTransactionGuid = "AUDITEDTRANSACTIONGUID_";

    // All fields below are effectively final, but cannot be declared so due to late initialisation.
    // TODO: use Stable Values (https://openjdk.org/jeps/502) once out of preview.
    private IAuditPropInstantiator<E> auditPropInstantiator;
    private IDomainMetadata domainMetadata;
    private Class<E> auditedEntityType;
    private fetch<E> fetchModelForAuditing;

    private PropertyInliner propInliner;

    private IUserProvider userProvider;
    /// Names of properties of the audited entity that are required to create an audit record.
    private Set<String> propertiesForAuditing;
    /// Key: audited property. Value: audit-property.
    private Map<String, String> auditedToAuditPropertyNames;

    @Inject
    protected void init(
            final AuditingMode auditingMode,
            final IUserProvider userProvider,
            final IAuditTypeFinder a3tFinder,
            final IDomainMetadata domainMetadata,
            final PropertyInliner propInliner,
            final ICompanionObjectFinder coFinder)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(this.getClass(), auditingMode);
        }

        this.userProvider = userProvider;
        this.domainMetadata = domainMetadata;
        this.propInliner = propInliner;
        auditedToAuditPropertyNames = makeAuditedToAuditPropertyNames(domainMetadata, getEntityType());
        propertiesForAuditing = collectPropertiesForAuditing(auditedToAuditPropertyNames.keySet());
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

            if (!dirtyProperties.isEmpty()) {
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

    static final String TEMPLATE_INSERT_FROM_SELECT_STMT = """
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

    /// A cache for SQL insert statements, used to insert audit records.
    /// The key is the audit entity type.
    private static final Cache<Class<?>, String> CACHE_SQL_INSERT_AUDIT_RECORD_STMT = CacheBuilder.newBuilder().weakKeys().initialCapacity(10).maximumSize(100).concurrencyLevel(50).build();

    /// Returns an insert SQL statement for inserting an audit record.
    ///
    static String sqlInsertAuditRecordStmt(
            final Class<? extends AbstractAuditEntity<?>> auditEntityType,
            final Class<? extends AbstractEntity<?>> auditedEntityType,
            final IDomainMetadata domainMetadata,
            final PropertyInliner propInliner,
            final String idCol,
            final String verCol,
            final String auditUserCol,
            final String auditDateCol,
            final String auditedEntityCol,
            final String auditedVersionCol,
            final String auditedTransactionGuid)
    {
        try {
            return CACHE_SQL_INSERT_AUDIT_RECORD_STMT.get(auditEntityType, () -> {
                final var auditedToAuditPropNames = makeAuditedToAuditPropertyNames(domainMetadata, auditEntityType);

                final var mdAuditEntity = domainMetadata.forEntity(auditEntityType);
                final var tblAuditEntity = mdAuditEntity.asPersistent().map(md -> md.data().tableName()).orElseThrow();
                final var mdAuditedEntity = domainMetadata.forEntity(auditedEntityType);
                final var tblAuditedEntity = mdAuditedEntity.asPersistent().map(md -> md.data().tableName()).orElseThrow();

                final var auditColumnNames = domainMetadata.forEntity(auditEntityType).properties().stream()
                                             .filter(pm -> auditedToAuditPropNames.containsValue(pm.name()))
                                             .sorted()
                                             .flatMap(p -> p.asPersistent().stream())
                                             .flatMap(pm -> propInliner.inline(pm).orElseGet(() -> List.of(pm)).stream())
                                             .map(pm -> pm.data().column().name).toList();
                final var auditedColumnNames = domainMetadata.forEntity(auditedEntityType).properties().stream()
                                               .filter(pm -> auditedToAuditPropNames.containsKey(pm.name()))
                                               .sorted()
                                               .flatMap(p -> p.asPersistent().stream())
                                               .flatMap(pm -> propInliner.inline(pm).orElseGet(() -> List.of(pm)).stream())
                                               .map(pm -> pm.data().column().name).toList();

                final var auditColumnsForSql = String.join(",\n    ", auditColumnNames);
                final var auditedColumnsForSql = StreamUtils.zip(auditedColumnNames, auditColumnNames, T2::t2)
                                                            .map(t -> t._1 + " AS " + t._2).collect(joining(",\n    "));

                return TEMPLATE_INSERT_FROM_SELECT_STMT.formatted(
                        tblAuditEntity, idCol, verCol, auditedEntityCol, auditedVersionCol, auditUserCol, auditDateCol, auditedTransactionGuid,
                        auditColumnsForSql, auditedColumnsForSql, tblAuditedEntity);

            });
        } catch (final Exception ex) {
            throw new EntityCompanionException("Could not create an audit insert statement for entity [%s].".formatted(auditEntityType.getSimpleName()), ex);
        }
    }

    /// Inserts a record of the audit entity as a copy of the audited entity, identified with `auditedEntityId` and `auditedEntityVersion`.
    ///
    /// @param auditedEntityId
    ///         the audited entity ID, used to identify a record to be audited
    /// @param transactionGuid
    ///         identifier of a transaction that was used to save the audited entity
    /// @return an ID of the audit entity that just inserted
    ///
    private Long insertAudit(final Long auditedEntityId,  final Long auditedEntityVersion, final String transactionGuid) {
        final String idCol = getDbVersion().idColumnName();
        final String verCol = getDbVersion().versionColumnName();

        final var insertStmt = sqlInsertAuditRecordStmt(getEntityType() /* audit entity type */, auditedEntityType,
                                                        domainMetadata, propInliner,
                                                        idCol, verCol,
                                                        auditUserCol, auditDateCol, auditedEntityCol, auditedVersionCol, auditedTransactionGuid);
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

    private int insertAuditProps(final Long auditedEntityId, final List<PropertyDescriptor<? extends AbstractSynAuditEntity<E>>> pdAuditProps) {
        final String smtp = auditPropInstantiator.sqlInsertAuditPropStmt();

        return getSession().doReturningWork(conn -> {
            try (final PreparedStatement ps = conn.prepareStatement(smtp)) {
                pdAuditProps.forEach(pd -> {
                    try {
                        final Long id = nextIdValue(ID_SEQUENCE_NAME, getSession());
                        ps.setLong(1, id);
                        ps.setLong(2, auditedEntityId);
                        ps.setString(3, pd.toString());
                        ps.addBatch();
                    } catch (final SQLException ex) {
                        throw new DbException(ERR_CREATING_INSERT_STMT.formatted(pd.getEntityType().getSimpleName()), ex);
                    }
                });
                final int[] batchCounts = ps.executeBatch();
                return java.util.Arrays.stream(batchCounts).sum();
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
