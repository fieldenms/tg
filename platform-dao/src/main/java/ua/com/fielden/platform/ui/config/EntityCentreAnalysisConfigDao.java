package ua.com.fielden.platform.ui.config;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IEntityCentreAnalysisConfig}.
 *
 * @author Developers
 *
 */
@EntityType(EntityCentreAnalysisConfig.class)
public class EntityCentreAnalysisConfigDao extends CommonEntityDao<EntityCentreAnalysisConfig> implements IEntityCentreAnalysisConfig {
    @Inject
    public EntityCentreAnalysisConfigDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public IPage<EntityCentreAnalysisConfig> findDetails(final EntityCentreConfig masterEntity, final fetch<EntityCentreAnalysisConfig> fetch, final int pageCapacity) {
        final EntityResultQueryModel<EntityCentreAnalysisConfig> selectModel = select(EntityCentreAnalysisConfig.class).where().prop("entityCentreConfig").eq().val(masterEntity).model();
        return firstPage(from(selectModel).with(orderBy().prop("title").asc().model()).with(fetch).model(), pageCapacity);
    }

    @Override
    public EntityCentreAnalysisConfig saveDetails(final EntityCentreConfig masterEntity, final EntityCentreAnalysisConfig detailEntity) {
        return save(detailEntity);
    }

    @Override
    public void deleteDetails(final EntityCentreConfig masterEntity, final EntityCentreAnalysisConfig detailEntity) {
        delete(detailEntity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityCentreAnalysisConfig entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<EntityCentreAnalysisConfig> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

}