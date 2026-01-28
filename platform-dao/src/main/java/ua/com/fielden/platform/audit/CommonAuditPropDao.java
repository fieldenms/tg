package ua.com.fielden.platform.audit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/// Base type for implementations of audit-prop entity companion objects.
///
/// Cannot be used if auditing is disabled — will throw [AuditingModeException] upon construction.
///
/// @param <E>  the audited entity type
///
public abstract class CommonAuditPropDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractAuditProp<E>>
        implements IAuditPropInstantiator<E>
{

    private Class<AbstractAuditEntity<E>> auditEntityType;
    private Class<AbstractSynAuditEntity<E>> synAuditEntityType;

    private IDomainMetadata domainMetadata;

    @Inject
    protected void init(
            final AuditingMode auditingMode,
            final IDomainMetadata domainMetadata,
            final IAuditTypeFinder auditTypeFinder)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(this.getClass(), auditingMode);
        }

        this.domainMetadata = domainMetadata;
        final var navigator = auditTypeFinder.navigateAuditProp(getEntityType());
        auditEntityType = navigator.auditEntityType();
        synAuditEntityType = navigator.synAuditEntityType();
    }

    private static final String TEMPLATE_INSERT_STMT = """
            INSERT INTO %1$s(%2$s, %3$s, %4$s, %5$s)
            VALUES (%6$s, 0, ?, ?);
            """;

    /// A cache for SQL insert statements, used to insert audit records.
    /// Keys are audit-prop entity types.
    private static final Cache<Class<?>, String> CACHE_SQL_INSERT_AUDIT_RECORD_STMT = CacheBuilder.newBuilder().weakKeys().initialCapacity(10).maximumSize(100).concurrencyLevel(50).build();

    @Override
    public String sqlInsertAuditPropStmt() {
        try {
            return CACHE_SQL_INSERT_AUDIT_RECORD_STMT.get(getEntityType(), () -> {
                final var mdAuditPropEntity = domainMetadata.forEntity(getEntityType());
                final var tblAuditPropEntity = mdAuditPropEntity.asPersistent().map(md -> md.data().tableName()).orElseThrow();
                final var cols = Stream.of(mdAuditPropEntity.property(AbstractAuditProp.AUDIT_ENTITY),
                                           mdAuditPropEntity.property(AbstractAuditProp.PROPERTY))
                        .sorted()
                        .flatMap(pm -> pm.asPersistent().stream())
                        .map(pm -> pm.data().column().name)
                        .toList();
                final String idCol = getDbVersion().idColumnName();
                final String verCol = getDbVersion().versionColumnName();
                return TEMPLATE_INSERT_STMT.formatted(tblAuditPropEntity, idCol, verCol, cols.getFirst(), cols.getLast(), getDbVersion().nextSequenceValSql());
            });
        } catch (final EntityCompanionException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new EntityCompanionException("Could not create an audit-prop insert statement for entity [%s].".formatted(auditEntityType.getSimpleName()), ex);
        }
    }

    @Override
    public PropertyDescriptor<? extends AbstractSynAuditEntity<E>> pd(final String propName) {
        return PropertyDescriptor.pd(synAuditEntityType, propName);
    }

    @Override
    public Class<? extends AbstractAuditProp<E>> auditPropEntityType() {
        return getEntityType();
    }

    @Override
    protected IFetchProvider<AbstractAuditProp<E>> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .filter(PropertyMetadata::isPersistent)
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

}
