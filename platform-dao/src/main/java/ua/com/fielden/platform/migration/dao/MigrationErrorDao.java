package ua.com.fielden.platform.migration.dao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.migration.MigrationError;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link MigrationError}.
 *
 * @author TG Team
 *
 */
@EntityType(MigrationError.class)
public class MigrationErrorDao extends CommonEntityDao<MigrationError> implements IMigrationErrorDao {

    @Inject
    protected MigrationErrorDao(final IFilter filter) {
	super(filter);
    }
}
