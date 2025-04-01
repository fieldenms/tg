package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/**
 * Base type for implementations of audit-entity companion objects.
 * <p>
 * Cannot be used if auditing is disabled. Will throw {@link AuditingModeException} upon construction.
 *
 * @param <E>  the audited entity type
 */
public abstract class CommonAuditEntityDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractAuditEntity<E>>
        implements IEntityAuditor<E>
{

    private static final int AUDIT_PROP_BATCH_SIZE = 100;

    // All fields below are effectively final, but cannot be declared so due to late initialisation.

    private Class<AbstractAuditProp<E>> auditPropType;
    private IDomainMetadata domainMetadata;
    private IEntityReader<E> coAuditedEntity;
    private fetch<E> fetchModelForAuditing;
    private EntityBatchInsertOperation.Factory batchInsertFactory;
    private IUserProvider userProvider;
    /**
     * Names of properties of the audited entity that are required to create an audit record.
     * These properties must not be proxied.
     */
    private Set<String> propertiesForAuditing;
    /**
     * A bidirectional mapping between names of audited and audit properties.
     * <p>
     * Standard direction: keys - audited properties, values - audit properties.
     * <p>
     * Inverse direction: keys - audit properties, values - audited properties.
     */
    private ImmutableBiMap<String, String> auditedToAuditPropertyNames;

    @Inject
    protected void init(
            final AuditingMode auditingMode,
            final EntityBatchInsertOperation.Factory batchInsertFactory,
            final IUserProvider userProvider,
            final IAuditTypeFinder a3tFinder,
            final IDomainMetadata domainMetadata,
            final ICompanionObjectFinder coFinder)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(this.getClass(), auditingMode);
        }

        this.batchInsertFactory = batchInsertFactory;
        this.userProvider = userProvider;
        this.domainMetadata = domainMetadata;
        auditPropType = a3tFinder.navigateAudit(getEntityType()).auditPropType();
        auditedToAuditPropertyNames = makeAuditedToAuditPropertyNames(domainMetadata);
        propertiesForAuditing = collectPropertiesForAuditing(auditedToAuditPropertyNames.keySet());
        fetchModelForAuditing = makeFetchModelForAuditing(a3tFinder.navigateAudit(getEntityType()).auditedType(), propertiesForAuditing, domainMetadata);
        coAuditedEntity = coFinder.find(a3tFinder.navigateAudit(getEntityType()).auditedType());
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

    /**
     * Returns the name of a property of this audit-entity type that audits the specified property of the audited entity type,
     * if the specified property is indeed audited; otherwise, returns {@code null}.
     */
    protected final @Nullable String getAuditPropertyName(final CharSequence auditedProperty) {
        return auditedToAuditPropertyNames.get(auditedProperty.toString());
    }

    /**
     * Returns the name of a property of the audited entity type that is audited by the specified property of this audit-entity type,
     * if the specified property is an audit property; otherwise, returns {@code null}.
     */
    protected final @Nullable String getAuditedPropertyName(final CharSequence auditProperty) {
        return auditedToAuditPropertyNames.inverse().get(auditProperty.toString());
    }

    @Override
    public void audit(final E auditedEntity, final String transactionGuid, final Iterable<? extends CharSequence> dirtyProperties) {
        // NOTE save() is annotated with SessionRequired.
        //      To truly enforce the contract of this method described in IAuditEntityDao, a version of save() without
        //      SessionRequired would need to be used.

        // Audit-entity may need to be refetched for its ID (and nothing else) is required to persist audit-prop entities below.
        final var refetchedAuditedEntity = refetchAuditedEntity(auditedEntity);
        final AbstractAuditEntity<E> auditEntity = save(newAudit(refetchedAuditedEntity, transactionGuid), Optional.of(fetchNone(getEntityType()).with(ID))).asRight().value();

        if (!Iterables.isEmpty(dirtyProperties)) {
            // Audit information about changed properites
            final IAuditPropInstantiator<E> coAuditProp = co(auditPropType);
            final boolean isNewAuditedEntity = refetchedAuditedEntity.getVersion() == 0L;
            final var auditProps = Streams.stream(dirtyProperties)
                    .map(property -> {
                        final var auditProperty = getAuditPropertyName(property);
                        // Ignore properties that are not audited.
                        // Ignore nulls if this is the very first version of the audited entity, which means that there are no historical values for its properties.
                        if (auditProperty != null && !(isNewAuditedEntity && refetchedAuditedEntity.get(property.toString()) == null)) {
                            // We can use the fast method because its arguments are known to be valid at this point.
                            return coAuditProp.fastNewAuditProp(auditEntity, auditProperty);
                        }
                        else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(toImmutableList());
            // Batch insertion is most helpful when saving the very first audit record (i.e., the audited entity is 'new'),
            // as it results in all assigned properties being audited.
            final var batchInsert = batchInsertFactory.create(() -> new TransactionalExecution(userProvider, this::getSession));
            batchInsert.batchInsert(auditProps, AUDIT_PROP_BATCH_SIZE);
        }
    }

    @Override
    public void audit(final E auditedEntity, final String transactionGuid) {
        final var dirtyProperties = auditedEntity.getDirtyProperties().stream().map(MetaProperty::getName).toList();
        audit(auditedEntity, transactionGuid, dirtyProperties);
    }

    /**
     * Returns a new, initialised instance of this audit-entity type.
     *
     * @param auditedEntity  the audited entity that will be used to initialise the audit-entity instance.
     *                       Must be persisted, non-dirty and contain all properties that are necessary for auditing.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     */
    private AbstractAuditEntity<E> newAudit(final E auditedEntity, final String transactionGuid) {
        if (auditedEntity.isDirty()) {
            throw failuref("Only persisted and non-dirty instances of [%s] can be audited.", auditedEntity.getType().getTypeName());
        }
        // TODO Assert that audited entity is valid?

        final AbstractAuditEntity<E> audit = new_();
        audit.beginInitialising();

        // properties common to all audit-entities
        audit.setAuditedEntity(auditedEntity);
        audit.setAuditedVersion(auditedEntity.getVersion());
        // Alternatively, annotate AbstractAuditEntity.auditDate with @IsProperty(assignBeforeSave = true)
        audit.setAuditDate(now().toDate());
        // Alternatively, annotate AbstractAuditEntity.user with @IsProperty(assignBeforeSave = true)
        audit.setUser(getUserOrThrow());
        audit.setAuditedTransactionGuid(transactionGuid);

        // specific, audited properties
        auditedToAuditPropertyNames.forEach((auditedProp, auditProp) -> audit.set(auditProp, auditedEntity.get(auditedProp)));

        audit.endInitialising();
        return audit;
    }

    private E refetchAuditedEntity(final E auditedEntity) {
        final var proxiedPropertyNames = auditedEntity.proxiedPropertyNames();
        return propertiesForAuditing.stream().anyMatch(proxiedPropertyNames::contains)
                ? coAuditedEntity.findById(auditedEntity.getId(), fetchModelForAuditing)
                : auditedEntity;
    }

    @Override
    protected IFetchProvider<AbstractAuditEntity<E>> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .filter(PropertyMetadata::isPersistent)
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

    /**
     * Returns the current user, if defined; otherwise, throws an exception.
     */
    private User getUserOrThrow() {
        final var user = getUser();
        if (user == null) {
            throw new EntityCompanionException("The current user is not defined.");
        }
        return user;
    }

    private ImmutableBiMap<String, String> makeAuditedToAuditPropertyNames(final IDomainMetadata domainMetadata) {
        final var auditEntityMetadata = domainMetadata.forEntity(getEntityType());
        final var builder = ImmutableBiMap.<String, String> builderWithExpectedSize(auditEntityMetadata.properties().size() - 6);
        auditEntityMetadata.properties()
                .stream()
                // Skip inactive audit properties.
                .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                .forEach(property -> {
                    final var auditedPropName = requireNonNull(AuditUtils.auditedPropertyName(property.name()));
                    builder.put(auditedPropName, property.name());
                });
        return builder.buildOrThrow();
    }

}
