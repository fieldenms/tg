package ua.com.fielden.platform.migration.rao;

import ua.com.fielden.platform.migration.MigrationRun;
import ua.com.fielden.platform.migration.controller.IMigrationRun;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO for {@link MigrationRun}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MigrationRun.class)
public class MigrationRunRao extends CommonEntityRao<MigrationRun> implements IMigrationRun {
    private static final long serialVersionUID = 1L;

    @Inject
    public MigrationRunRao(final RestClientUtil restUtil) {
        super(restUtil);
    }
}
