package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgISStatusActivationFunctionalEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgISStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgISStatusActivationFunctionalEntity.class)
public class TgISStatusActivationFunctionalEntityDao extends CommonEntityDao<TgISStatusActivationFunctionalEntity> implements ITgISStatusActivationFunctionalEntity {

    private final TgISStatusActivationFunctionalEntityMixin mixin;
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgISStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;

        mixin = new TgISStatusActivationFunctionalEntityMixin(this);
    }

    @Override
    @SessionRequired
    public TgISStatusActivationFunctionalEntity save(final TgISStatusActivationFunctionalEntity entity) {
        for (final AbstractEntity<?> selectedEntity : entity.getContext().getSelectedEntities()) { // should be only single instance
            final TgPersistentEntityWithProperties selected = masterEntityCo.findById(selectedEntity.getId(), fetchAll(TgPersistentEntityWithProperties.class));
            selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "IS"));
            masterEntityCo.save(selected);
        }
        return super.save(entity);
    }

}