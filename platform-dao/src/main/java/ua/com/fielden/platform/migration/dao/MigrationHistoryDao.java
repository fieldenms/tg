package ua.com.fielden.platform.migration.dao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.migration.MigrationHistory;
import ua.com.fielden.platform.migration.controller.IMigrationHistory;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link MigrationHistory}.
 * 
 * @author TG Team
 */
@EntityType(MigrationHistory.class)
public class MigrationHistoryDao extends CommonEntityDao<MigrationHistory> implements IMigrationHistory {

    @Inject
    protected MigrationHistoryDao(final IFilter filter) {
        super(filter);
    }

}
