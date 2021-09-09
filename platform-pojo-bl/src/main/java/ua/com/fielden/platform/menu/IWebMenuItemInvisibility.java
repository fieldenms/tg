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
public interface IWebMenuItemInvisibility extends IEntityDao<WebMenuItemInvisibility> {

    /**
     * Removes the list of {@link WebMenuItemInvisibility}s from data base
     *
     * @param associations
     */
    void removeAssociation(Set<WebMenuItemInvisibility> associations);

    static final IFetchProvider<WebMenuItemInvisibility> FETCH_PROVIDER = EntityUtils.fetch(WebMenuItemInvisibility.class)
            .with("menuItemUri")
            .with("owner", "owner.base");
}