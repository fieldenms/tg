package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgStatusActivationFunctionalEntity.class)
public class TgStatusActivationFunctionalEntityDao extends CommonEntityDao<TgStatusActivationFunctionalEntity> implements ITgStatusActivationFunctionalEntity {
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;
    }

    @Override
    @SessionRequired
    public TgStatusActivationFunctionalEntity save(final TgStatusActivationFunctionalEntity entity) {
        final TgPersistentEntityWithProperties selected = masterEntityCo.findById(entity.getSelectedEntityId(), fetchAll(TgPersistentEntityWithProperties.class));
        selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "DR"));
        masterEntityCo.save(selected);
        return super.save(entity);
    }
}