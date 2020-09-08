package ua.com.fielden.platform.menu;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;


/**
 * DAO implementation for companion object {@link IMenuSaveAction}.
 *
 * @author Developers
 *
 */
@EntityType(MenuSaveAction.class)
public class MenuSaveActionDao extends CommonEntityDao<MenuSaveAction> implements IMenuSaveAction {

    private final Logger logger = getLogger(this.getClass());

    private final IUserProvider userProvider;

    @Inject
    public MenuSaveActionDao(final IFilter filter, final IUserProvider userProvider) {
        super(filter);
        this.userProvider = userProvider;
    }

    @Override
    @SessionRequired
    public MenuSaveAction save(final MenuSaveAction entity) {
        if (userProvider.getUser().isBase()) {
            final IWebMenuItemInvisibility coMenuInvisibility = co$(WebMenuItemInvisibility.class);
            if (!entity.getInvisibleMenuItems().isEmpty()) {
                entity.getInvisibleMenuItems().forEach(menuItem -> {
                    try {
                        coMenuInvisibility.save(getEntityFactory().newByKey(WebMenuItemInvisibility.class, userProvider.getUser(), menuItem));
                    } catch (final EntityCompanionException e) {
                        logger.error(e.getMessage());
                    }
                });
            }
            if (!entity.getVisibleMenuItems().isEmpty()) {
                final EntityResultQueryModel<WebMenuItemInvisibility> model = select(WebMenuItemInvisibility.class).where()//
                .prop("owner").eq().val(userProvider.getUser()).and()//
                .prop("menuItemUri").in().values(entity.getVisibleMenuItems().toArray(new String[0])).model();
                coMenuInvisibility.batchDelete(model);
            }
        }
        return entity;
    }
}