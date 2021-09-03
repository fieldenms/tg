package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKeyFor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
/**
 * DAO implementation for companion object {@link IWebMenuItemInvisibility}.
 *
 * @author Developers
 *
 */
@EntityType(WebMenuItemInvisibility.class)
public class WebMenuItemInvisibilityDao extends CommonEntityDao<WebMenuItemInvisibility> implements IWebMenuItemInvisibility {

    @Inject
    public WebMenuItemInvisibilityDao(final IFilter filter) {
        super(filter);
    }

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
    protected IFetchProvider<WebMenuItemInvisibility> createFetchProvider() {
        // TODO: uncomment the following line and specify the properties, which are required for the UI
        // return super.createFetchProvider().with("key", "desc");
        throw new UnsupportedOperationException("Please specify the properties, which are required for the UI in WebMenuItemInvisibilityDao.createFetchProvider()");
    }

    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<WebMenuItemInvisibility> model) {
        return defaultBatchDelete(model);
    }

    @Override
    public void removeAssociation(final Set<WebMenuItemInvisibility> associations) {
        createQueryByKeyFor(getDbVersion(), getEntityType(), getKeyType(), associations).map(this::batchDelete);
    }

}