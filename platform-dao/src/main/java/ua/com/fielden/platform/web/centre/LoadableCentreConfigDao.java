package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link LoadableCentreConfigCo}.
 *
 * @author TG Team
 */
@EntityType(LoadableCentreConfig.class)
public class LoadableCentreConfigDao extends CommonEntityDao<LoadableCentreConfig> implements LoadableCentreConfigCo {

}
