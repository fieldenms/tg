package ua.com.fielden.platform.ui.config.controller;

import static ua.com.fielden.platform.equery.equery.select;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IMainMenuItemInvisibilityController}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MainMenuItemInvisibility.class)
public class MainMenuItemInvisibilityControllerDao extends CommonEntityDao<MainMenuItemInvisibility> implements IMainMenuItemInvisibilityController {

    private final EntityFactory factory;

    @Inject
    protected MainMenuItemInvisibilityControllerDao(final IFilter filter, final EntityFactory factory) {
	super(filter);
	this.factory = factory;
    }

    @Override
    @SessionRequired
    public void delete(final MainMenuItemInvisibility entity) {
	super.defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final IQueryOrderedModel<MainMenuItemInvisibility> model) {
	super.defaultDelete(model);
    }

    @Override
    public void makeInvisible(final MainMenuItem menuItem, final User user) {
	final MainMenuItemInvisibility invisibility = factory.newByKey(MainMenuItemInvisibility.class, user, menuItem);
	save(invisibility);

    }

    @Override
    public void makeVisible(final MainMenuItem menuItem, final User user) {
	final IQueryModel<MainMenuItemInvisibility> model = select(MainMenuItemInvisibility.class).where()//
	.prop("owner").eq().val(user).and()//
	.prop("menuItem").eq().val(menuItem).model();

	delete(model);
    }

}
