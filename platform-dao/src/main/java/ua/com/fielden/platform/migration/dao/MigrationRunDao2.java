package ua.com.fielden.platform.migration.dao;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.migration.MigrationRun;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link MigrationRun}.
 *
 * @author TG Team
 */
@EntityType(MigrationRun.class)
public class MigrationRunDao2 extends CommonEntityDao2<MigrationRun> implements IMigrationRunDao2{

    @Inject
    protected MigrationRunDao2(final IFilter filter) {
	super(filter);
    }
}
