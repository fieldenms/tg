package ua.com.fielden.platform.menu;

import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link WebMenuItemInvisibility}.
 *
 * @author TG Team
 *
 */
public interface WebMenuItemInvisibilityCo extends IEntityDao<WebMenuItemInvisibility> {

    static final IFetchProvider<WebMenuItemInvisibility> FETCH_PROVIDER = EntityUtils.fetch(WebMenuItemInvisibility.class)
            .with("menuItemUri")
            .with("owner", "owner.base");

    /**
     * Deletes all specified {@link WebMenuItemInvisibility}s.
     *
     * @param associations
     */
    void deleteAssociation(final Set<WebMenuItemInvisibility> associations);

}