package ua.com.fielden.platform.sample.domain;

import java.util.Collection;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgEntityWithPropertyDependency}.
 *
 * @author Developers
 *
 */
@EntityType(TgEntityWithPropertyDependency.class)
public class TgEntityWithPropertyDependencyDao extends CommonEntityDao<TgEntityWithPropertyDependency> implements ITgEntityWithPropertyDependency {
    @Inject
    public TgEntityWithPropertyDependencyDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public IFetchProvider<TgEntityWithPropertyDependency> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("property", "dependentProp")
                .with("propX", "propY", "prop1", "prop2");
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
}