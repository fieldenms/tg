package ua.com.fielden.platform.menu;

import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;

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
}