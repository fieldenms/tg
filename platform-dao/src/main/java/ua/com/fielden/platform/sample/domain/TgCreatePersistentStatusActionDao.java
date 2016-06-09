package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DAO implementation for companion object {@link ITgCreatePersistentStatusAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCreatePersistentStatusAction.class)
public class TgCreatePersistentStatusActionDao extends CommonEntityDao<TgCreatePersistentStatusAction> implements ITgCreatePersistentStatusAction {

    private final ITgPersistentStatus coTgPersistentStatus;

    @Inject
    public TgCreatePersistentStatusActionDao(final ITgPersistentStatus coTgPersistentStatus, final IFilter filter) {
        super(filter);
        this.coTgPersistentStatus = coTgPersistentStatus;
    }

    @Override
    @SessionRequired
    public TgCreatePersistentStatusAction save(final TgCreatePersistentStatusAction entity) {
        final Result res = entity.isValid();
        if (!res.isSuccessful()) {
            throw res;
        }
        
        
        final TgPersistentStatus newStatus = coTgPersistentStatus
                .save(entity.getEntityFactory().newEntity(TgPersistentStatus.class, entity.getStatusCode(), entity.getDesc()));
        
        return entity.setStatus(newStatus);
    }

}