package ua.com.fielden.platform.migration.dao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.migration.MigrationError;
import ua.com.fielden.platform.migration.controller.IMigrationError;

import com.google.inject.Inject;

/**
 * DAO for {@link MigrationError}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MigrationError.class)
public class MigrationErrorDao extends CommonEntityDao<MigrationError> implements IMigrationError {

    @Inject
    protected MigrationErrorDao(final IFilter filter) {
        super(filter);
    }
}
