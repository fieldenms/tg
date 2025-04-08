package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.functional.PersistentEntityInfo_CanExecute_Token;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentWithAuditData;

///
/// Producer of {@link PersistentEntityInfo} that retrieve the versioning information for an entity that is passed as the "current entity" in the context.
///
public class PersistentEntityInfoProducer extends DefaultEntityProducerWithContext<PersistentEntityInfo> {

    public static final String ERR_NOT_SUITABLE_ENTITY = "Current entity [%s] does not have the versioning info.";

    @Inject
    public PersistentEntityInfoProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, PersistentEntityInfo.class, companionFinder);
    }

    @Override
    @Authorise(PersistentEntityInfo_CanExecute_Token.class)
    protected PersistentEntityInfo provideDefaultValues(final PersistentEntityInfo entity) {
        if (currentEntityNotEmpty()) {
            final AbstractEntity<?> currEntity = currentEntity();
            if (isPersistentWithAuditData(currEntity.getType())) {
                final var entityCo = (IEntityDao<AbstractPersistentEntity<?>>) co(currEntity.getType());
                final var entityWithInfo = entityCo.findById(currEntity.getId(), fetchKeyAndDescOnly(entityCo.getEntityType())
                        .with(AbstractPersistentEntity.VERSION)
                        .with(AbstractPersistentEntity.CREATED_BY)
                        .with(AbstractPersistentEntity.CREATED_DATE)
                        .with(AbstractPersistentEntity.LAST_UPDATED_BY)
                        .with(AbstractPersistentEntity.LAST_UPDATED_DATE));
                entity.setEntityId(entityWithInfo.getId())
                        .setEntityVersion(entityWithInfo.getVersion())
                        .setCreatedBy(entityWithInfo.getCreatedBy())
                        .setCreatedDate(entityWithInfo.getCreatedDate())
                        .setLastUpdatedBy(entityWithInfo.getLastUpdatedBy())
                        .setLastUpdatedDate(entityWithInfo.getLastUpdatedDate())
                        .setEntityTitle(isEmpty(entityWithInfo.getDesc()) ? "%s".formatted(entityWithInfo.getKey()) : "%s: %s".formatted(entityWithInfo.getKey(), entityWithInfo.getDesc()));
                return super.provideDefaultValues(entity);
            }
            else {
                throw new InvalidStateException(ERR_NOT_SUITABLE_ENTITY.formatted(currEntity.getType().getSimpleName()));
            }
        }
        // This happens when the entity master gets closed.
        else {
            return super.provideDefaultValues(entity);
        }
    }

}
