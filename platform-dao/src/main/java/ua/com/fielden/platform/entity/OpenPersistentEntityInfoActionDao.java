package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.reflection.ClassesRetriever;

import java.util.HashMap;
import java.util.Map;

import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.entity.OpenPersistentEntityInfoAction.AUDIT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// DAO implementation for the {@link OpenPersistentEntityInfoActionCo} companion object.
///
@EntityType(OpenPersistentEntityInfoAction.class)
public class OpenPersistentEntityInfoActionDao extends AbstractOpenCompoundMasterDao<OpenPersistentEntityInfoAction>  implements OpenPersistentEntityInfoActionCo {

    private final IAuditTypeFinder auditTypeFinder;

    @Inject
    OpenPersistentEntityInfoActionDao(final IFilter filter, final IEntityAggregatesOperations coAggregates, final IAuditTypeFinder auditTypeFinder) {
        super(filter, coAggregates);
        this.auditTypeFinder = auditTypeFinder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OpenPersistentEntityInfoAction save(final OpenPersistentEntityInfoAction entity) {
        final var savedEntity = super.save(entity);
        final var auditedType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entity.getKey().getEntityType());
        final var synAuditType = auditTypeFinder.navigate(auditedType).synAuditEntityType();
        final var query = select()
                .yield()
                    .caseWhen()
                    .exists(enhanceEmbededCentreQuery(select(synAuditType).where(), AUDITED_ENTITY, entity.getKey().getEntityId())
                                    .model()
                                    .setFilterable(true))
                    .then().val(1).otherwise().val(0)
                    .endAsInt()
                .as(AUDIT)
                .modelAsAggregate();
        final EntityAggregates result = coAggregates.getEntity(from(query).model());
        final Map<String, Integer> newPresence = new HashMap<>(1);
        newPresence.put(AUDIT, result.get(AUDIT));
        savedEntity.setEntityPresence(newPresence);
        return savedEntity;
    }

    @Override
    protected IFetchProvider<OpenPersistentEntityInfoAction> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}
