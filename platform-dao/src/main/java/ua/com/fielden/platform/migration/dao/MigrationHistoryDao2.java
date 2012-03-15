package ua.com.fielden.platform.migration.dao;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.migration.MigrationHistory;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link MigrationHistory}.
 *
 * @author TG Team
 */
@EntityType(MigrationHistory.class)
public class MigrationHistoryDao2 extends CommonEntityDao2<MigrationHistory> implements IMigrationHistoryDao2 {

    @Inject
    protected MigrationHistoryDao2(final IFilter filter) {
	super(filter);
    }

}
