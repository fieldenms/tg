package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.migration.MigrationHistoryCo;
import ua.com.fielden.platform.migration.MigrationHistory;

import com.google.inject.Inject;

/**
 * DAO for {@link MigrationHistory}.
 * 
 * @author TG Team
 */
@EntityType(MigrationHistory.class)
public class MigrationHistoryDao extends CommonEntityDao<MigrationHistory> implements MigrationHistoryCo {

    @Inject
    protected MigrationHistoryDao(final IFilter filter) {
        super(filter);
    }

}
