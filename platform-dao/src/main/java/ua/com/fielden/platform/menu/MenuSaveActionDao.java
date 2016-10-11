package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibility;

import com.google.inject.Inject;


/**
 * DAO implementation for companion object {@link IMenuSaveAction}.
 *
 * @author Developers
 *
 */
@EntityType(MenuSaveAction.class)
public class MenuSaveActionDao extends CommonEntityDao<MenuSaveAction> implements IMenuSaveAction {

    private final Logger logger = Logger.getLogger(this.getClass());

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
            final IMainMenuItemInvisibility coMenuInvisibility = co(MainMenuItemInvisibility.class);
            if (!entity.getInvisibleMenuItems().isEmpty()) {
                entity.getInvisibleMenuItems().forEach(menuItem -> {
                    try {
                        coMenuInvisibility.save(getEntityFactory().newByKey(MainMenuItemInvisibility.class, userProvider.getUser(), menuItem));
                    } catch (final EntityCompanionException e) {
                        logger.error(e.getMessage());
                    }
                });
            }
            if (!entity.getVisibleMenuItems().isEmpty()) {
                final EntityResultQueryModel<MainMenuItemInvisibility> model = select(MainMenuItemInvisibility.class).where()//
                .prop("owner").eq().val(userProvider.getUser()).and()//
                .prop("menuItemUri").in().values(entity.getVisibleMenuItems().toArray(new String[0])).model();
                coMenuInvisibility.batchDelete(model);
            }
        }
        return entity;
    }
}