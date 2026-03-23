package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKeyFor;

/// DAO implementation for companion object [WebMenuItemInvisibilityCo].
///
@EntityType(WebMenuItemInvisibility.class)
public class WebMenuItemInvisibilityDao extends CommonEntityDao<WebMenuItemInvisibility> implements WebMenuItemInvisibilityCo {

    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    public int batchDelete(final List<WebMenuItemInvisibility> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    public IFetchProvider<WebMenuItemInvisibility> createFetchProvider() {
        return WebMenuItemInvisibilityCo.FETCH_PROVIDER;
    }

    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<WebMenuItemInvisibility> model) {
        return defaultBatchDelete(model);
    }

    @Override
    public void deleteAssociation(final Set<WebMenuItemInvisibility> associations) {
        createQueryByKeyFor(getDbVersion(), getEntityType(), getKeyType(), associations).map(this::batchDelete);
    }

    @Override
    @SessionRequired
    public int batchInsert(final Stream<WebMenuItemInvisibility> newEntities, final int batchSize) {
        return super.defaultBatchInsert(newEntities, batchSize);
    }
}