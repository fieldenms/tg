package ua.com.fielden.platform.ui.config;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.dao.exceptions.EntityDeletionException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/**
 * DAO implementation of {@link MainMenuItemCo}.
 *
 * @author TG Team
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemDao extends CommonEntityDao<MainMenuItem> implements MainMenuItemCo {

    private static final Logger LOGGER = getLogger(MainMenuItemDao.class);

    @Override
    public MainMenuItem new_() {
        return super.new_().setOrder(0);
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
            } catch (final javax.persistence.PersistenceException ex) {
                final var msg = ex.getCause() instanceof ConstraintViolationException
                        ? ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES
                        : ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS.formatted(ex.getMessage());
                LOGGER.error(msg, ex);
                throw new EntityDeletionException(msg, ex.getCause());
            }
        } else {
            defaultDelete(model, paramValues);
        }
    }

}
