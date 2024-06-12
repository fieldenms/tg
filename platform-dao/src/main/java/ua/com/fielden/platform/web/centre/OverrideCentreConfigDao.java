package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link OverrideCentreConfigCo}.
 *
 * @author TG Team
 */
@EntityType(OverrideCentreConfig.class)
public class OverrideCentreConfigDao extends CommonEntityDao<OverrideCentreConfig> implements OverrideCentreConfigCo {

}
