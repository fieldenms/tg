package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;

import jakarta.annotation.Nullable;
import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
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

    private static final String ERR_AUDIT_PROPERTY_UNEXPECTED_NAME = "Audit-property [%s.%s] has unexpected name.";
    static final String ERR_ONLY_PERSISTED_INSTANCES_CAN_BE_AUDITED = "Only persisted instances can be audited.";
    static final String ERR_ONLY_NON_DIRTY_INSTANCES_CAN_BE_AUDITED = "Only non-dirty instances can be audited.";

    private static final int AUDIT_PROP_BATCH_SIZE = 100;

    // All fields below are effectively final, but cannot be declared so due to late initialisation.

    private IAuditPropInstantiator<E> auditPropInstantiator;
    private IDomainMetadata domainMetadata;
    private IEntityReader<E> coAuditedEntity;
    private fetch<E> fetchModelForAuditing;
    private EntityBatchInsertOperation.Factory batchInsertFactory;
    private IUserProvider userProvider;
    /** Names of properties of the audited entity that are required to create an audit record. */
    private Set<String> propertiesForAuditing;
    /** Key: audited property. Value: audit-property. */
    private Map<String, String> auditedToAuditPropertyNames;

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
        auditedToAuditPropertyNames = makeAuditedToAuditPropertyNames(domainMetadata);
        propertiesForAuditing = collectPropertiesForAuditing(auditedToAuditPropertyNames.keySet());
        fetchModelForAuditing = makeFetchModelForAuditing(a3tFinder.navigateAudit(getEntityType()).auditedType(), propertiesForAuditing, domainMetadata);
        coAuditedEntity = coFinder.find(a3tFinder.navigateAudit(getEntityType()).auditedType());
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

    /**
     * Returns the name of a property of this audit-entity type that audits the specified property of the audited entity type,
     * if the specified property is indeed audited; otherwise, returns {@code null}.
     */
    private @Nullable String getAuditPropertyName(final CharSequence auditedProperty) {
        return auditedToAuditPropertyNames.get(auditedProperty.toString());
    }

    @Override
    public void audit(final E auditedEntity, final String transactionGuid, final Collection<String> dirtyProperties) {
        // NOTE save() is annotated with SessionRequired.
        //      To truly enforce the contract of this method described in IAuditEntityDao, a version of save() without
        //      SessionRequired would need to be used.

        if (!auditedEntity.isPersisted()) {
            throw cannotBeAuditedFailure(auditedEntity, ERR_ONLY_PERSISTED_INSTANCES_CAN_BE_AUDITED);
        }
        if (auditedEntity.isDirty()) {
            throw cannotBeAuditedFailure(auditedEntity, ERR_ONLY_NON_DIRTY_INSTANCES_CAN_BE_AUDITED);
        }

        // NOTE: It is possible for a persisted entity to become invalid upon retrieval from a persistent store
        //       (e.g., due to complex business logic in property definers), which may occur when an entity is refetched after being saved.
        //       We cannot meaningfully distinguish such edge cases from truly invalid states.
        //       However, as this method should only be used within the save operation, truly invalid states should never occur,
        //       since only valid instances can be saved.
        //       Therefore, we can skip the assertion about the audited entity validity.
        //       The working principle is "that which was persisted is valid and can be audited".
        //
        // if (!auditedEntity.isValid().isSuccessful()) {
        //     throw auditedEntity.isValid();
        // }

        final var anyAuditedPropertyDirty = auditedPropertyNames().stream().anyMatch(dirtyProperties::contains);

        if (anyAuditedPropertyDirty) {
            // Audit-entity may need to be refetched for its ID (and nothing else) is required to persist audit-prop entities below.
            final var refetchedAuditedEntity = refetchAuditedEntity(auditedEntity);
            final AbstractAuditEntity<E> auditEntity = save(newAudit(refetchedAuditedEntity, transactionGuid), Optional.of(fetchNone(getEntityType()).with(ID))).asRight().value();

            if (!dirtyProperties.isEmpty()) {
                // Audit information about changed properites
                final boolean isNewAuditedEntity = refetchedAuditedEntity.getVersion() == 0L;
                final var auditProps = dirtyProperties.stream()
                        .map(property -> {
                            final var auditProperty = getAuditPropertyName(property);
                            // Ignore properties that are not audited.
                            // Ignore nulls if this is the very first version of the audited entity, which means that there are no historical values for its properties.
                            if (auditProperty != null && !(isNewAuditedEntity && refetchedAuditedEntity.get(property) == null)) {
                                // We can use the fast method because its arguments are known to be valid at this point.
                                return auditPropInstantiator.fastNewAuditProp(auditEntity, auditProperty);
                            }
                            else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(toImmutableList());
                if (!auditProps.isEmpty()) {
                    // Batch insertion is most helpful when saving the very first audit record (i.e., the audited entity is 'new'),
                    // as it results in all assigned properties being audited.
                    final var batchInsert = batchInsertFactory.create(() -> new TransactionalExecution(userProvider, this::getSession));
                    batchInsert.batchInsert(auditProps, AUDIT_PROP_BATCH_SIZE);
                }
            }
        }
    }

    private Result cannotBeAuditedFailure(final E auditedEntity, final String format, final Object... args) {
        return failuref(String.join(".",
                                    "%s [%s] cannot be audited".formatted(getEntityTitle(getEntityType()), auditedEntity),
                                    format.formatted(args)));
    }

    /**
     * Returns a new, initialised instance of this audit-entity type.
     *
     * @param auditedEntity  the audited entity that will be used to initialise the audit-entity instance.
     *                       Must be persisted, non-dirty and contain all properties that are necessary for auditing.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     */
    private AbstractAuditEntity<E> newAudit(final E auditedEntity, final String transactionGuid) {
        final AbstractAuditEntity<E> audit = new_();
        audit.beginInitialising();

        // properties common to all audit-entities
        audit.setAuditedEntity(auditedEntity);
        audit.setAuditedVersion(auditedEntity.getVersion());
        audit.setAuditDate(now().toDate());
        // TODO `User` itself cannot be audited, because saving the first User involves its auditing, which in turn requires a persisted User for the `user` property.
        //      The current workaround is to persist the first User without auditing (e.g., via an SQL script).
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

    private Collection<String> auditedPropertyNames() {
        return auditedToAuditPropertyNames.keySet();
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

    private Map<String, String> makeAuditedToAuditPropertyNames(final IDomainMetadata domainMetadata) {
        final var auditEntityMetadata = domainMetadata.forEntity(getEntityType());

        return auditEntityMetadata.properties()
                .stream()
                // Skip inactive audit properties.
                .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                .collect(toImmutableMap(p -> Optional.ofNullable(AuditUtils.auditedPropertyName(p.name()))
                                                .orElseThrow(() -> new EntityDefinitionException(format(
                                                        ERR_AUDIT_PROPERTY_UNEXPECTED_NAME,
                                                        auditEntityMetadata.javaType().getSimpleName(), p.name()))),
                                        PropertyMetadata::name));
    }

}
