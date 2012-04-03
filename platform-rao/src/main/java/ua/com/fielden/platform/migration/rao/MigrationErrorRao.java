package ua.com.fielden.platform.migration.rao;

import ua.com.fielden.platform.migration.MigrationError;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao2;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO for {@link MigrationError}.
 *
 * @author TG Team
 *
 */
@EntityType(MigrationError.class)
public class MigrationErrorRao extends CommonEntityRao<MigrationError> implements IMigrationErrorDao2 {
    private static final long serialVersionUID = 1L;

    @Inject
    public MigrationErrorRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
