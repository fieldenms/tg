package ua.com.fielden.platform.migration.dao;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.migration.MigrationError;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link MigrationError}.
 *
 * @author TG Team
 *
 */
@EntityType(MigrationError.class)
public class MigrationErrorDao2 extends CommonEntityDao2<MigrationError> implements IMigrationErrorDao2 {

    @Inject
    protected MigrationErrorDao2(final IFilter filter) {
	super(filter);
    }
}
