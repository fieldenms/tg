package ua.com.fielden.platform.migration.rao;

import ua.com.fielden.platform.migration.MigrationHistory;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO for {@link MigrationHistory}.
 *
 * @author TG Team
 *
 */
@EntityType(MigrationHistory.class)
public class MigrationHistoryRao extends CommonEntityRao<MigrationHistory> implements IMigrationHistoryDao {
    private static final long serialVersionUID = 1L;

    @Inject
    public MigrationHistoryRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
