package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgSRStatusActivationFunctionalEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgSRStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgSRStatusActivationFunctionalEntity.class)
public class TgSRStatusActivationFunctionalEntityDao extends CommonEntityDao<TgSRStatusActivationFunctionalEntity> implements ITgSRStatusActivationFunctionalEntity {

    private final TgSRStatusActivationFunctionalEntityMixin mixin;
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgSRStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;

        mixin = new TgSRStatusActivationFunctionalEntityMixin(this);
    }

    @Override
    @SessionRequired
    public TgSRStatusActivationFunctionalEntity save(final TgSRStatusActivationFunctionalEntity entity) {
        for (final AbstractEntity<?> selectedEntity : entity.getContext().getSelectedEntities()) { // should be only single instance
            final TgPersistentEntityWithProperties selected = masterEntityCo.findById(selectedEntity.getId(), fetchAll(TgPersistentEntityWithProperties.class));
            selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "SR"));
            masterEntityCo.save(selected);
        }
        return super.save(entity);
    }

}