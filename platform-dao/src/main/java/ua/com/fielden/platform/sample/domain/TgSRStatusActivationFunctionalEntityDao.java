package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgSRStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgSRStatusActivationFunctionalEntity.class)
public class TgSRStatusActivationFunctionalEntityDao extends CommonEntityDao<TgSRStatusActivationFunctionalEntity> implements ITgSRStatusActivationFunctionalEntity {
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgSRStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;
    }

    @Override
    @SessionRequired
    public TgSRStatusActivationFunctionalEntity save(final TgSRStatusActivationFunctionalEntity entity) {
        final TgPersistentEntityWithProperties selected = masterEntityCo.findById(entity.getSelectedEntityId(), fetchAll(TgPersistentEntityWithProperties.class));
        selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "SR"));
        masterEntityCo.save(selected);
        return super.save(entity);
    }

}