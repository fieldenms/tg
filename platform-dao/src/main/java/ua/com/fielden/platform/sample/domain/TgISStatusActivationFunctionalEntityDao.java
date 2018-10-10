package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgISStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgISStatusActivationFunctionalEntity.class)
public class TgISStatusActivationFunctionalEntityDao extends CommonEntityDao<TgISStatusActivationFunctionalEntity> implements ITgISStatusActivationFunctionalEntity {
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgISStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;
    }

    @Override
    @SessionRequired
    public TgISStatusActivationFunctionalEntity save(final TgISStatusActivationFunctionalEntity entity) {
        final TgPersistentEntityWithProperties selected = masterEntityCo.findById(entity.getSelectedEntityId(), fetchAll(TgPersistentEntityWithProperties.class));
        selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "IS"));
        masterEntityCo.save(selected);
        return super.save(entity);
    }

}