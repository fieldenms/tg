package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgONStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgONStatusActivationFunctionalEntity.class)
public class TgONStatusActivationFunctionalEntityDao extends CommonEntityDao<TgONStatusActivationFunctionalEntity> implements ITgONStatusActivationFunctionalEntity {
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgONStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;
    }

    @Override
    @SessionRequired
    public TgONStatusActivationFunctionalEntity save(final TgONStatusActivationFunctionalEntity entity) {
        final TgPersistentEntityWithProperties selected = masterEntityCo.findById(entity.getSelectedEntityId(), fetchAll(TgPersistentEntityWithProperties.class));
        selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "ON"));
        masterEntityCo.save(selected);
        return super.save(entity);
    }

}