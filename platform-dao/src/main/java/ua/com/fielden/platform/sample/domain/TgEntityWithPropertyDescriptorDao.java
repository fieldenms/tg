package ua.com.fielden.platform.sample.domain;

import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgEntityWithPropertyDescriptorCo}.
 *
 * @author Developers
 */
@EntityType(TgEntityWithPropertyDescriptor.class)
public class TgEntityWithPropertyDescriptorDao extends CommonEntityDao<TgEntityWithPropertyDescriptor> implements TgEntityWithPropertyDescriptorCo {

    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    public int batchDelete(final List<TgEntityWithPropertyDescriptor> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    protected IFetchProvider<TgEntityWithPropertyDescriptor> createFetchProvider() {
        return super.createFetchProvider().with("propertyDescriptor");
    }

}
