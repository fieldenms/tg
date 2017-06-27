package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ICentreColumnWidthConfigUpdater}.
 *
 * @author Developers
 *
 */
@EntityType(CentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdaterDao extends CommonEntityDao<CentreColumnWidthConfigUpdater> implements ICentreColumnWidthConfigUpdater {

    @Inject
    public CentreColumnWidthConfigUpdaterDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
    }

    @Override
    @SessionRequired
    public CentreColumnWidthConfigUpdater save(final CentreColumnWidthConfigUpdater action) {
        action.getContext().getSelectionCrit().columnWidthAdjuster().accept(action.getPropWidths(), action.getPropGrows());
        return super.save(action);
    }
}
