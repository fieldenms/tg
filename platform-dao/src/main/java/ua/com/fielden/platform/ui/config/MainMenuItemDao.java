package ua.com.fielden.platform.ui.config;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;

/**
 * DAO implementation of {@link MainMenuItemCo}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemDao extends CommonEntityDao<MainMenuItem> implements MainMenuItemCo {
    @Inject
    protected MainMenuItemDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final MainMenuItem entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<MainMenuItem> model, final Map<String, Object> paramValues) {
        if (model.equals(select(MainMenuItem.class).model())) {
            // IMPORTANT : THIS IS DANGEROUS, ALL ITEMS IS TRYING TO BE DELETED!

            // perform deletion more effectively (not one by one, as defaultDelete() does, but in one bunch)
            final List<MainMenuItem> withParents = getAllEntities(
                    from(select(MainMenuItem.class).where().prop("parent").isNotNull().model())
                    .with(fetchAndInstrument(MainMenuItem.class).with("parent")).model());

            for (final MainMenuItem mmi : withParents) {
                mmi.setParent(null); // remove dependency
                save(mmi);
            }
            try {
                getSession().createQuery("delete " + getEntityType().getName()).executeUpdate();
            } catch (final ConstraintViolationException e) {
                throw new Result(new IllegalStateException("This entity could not be deleted due to existing dependencies."));
            }
        } else {
            defaultDelete(model, paramValues);
        }
    }
}
