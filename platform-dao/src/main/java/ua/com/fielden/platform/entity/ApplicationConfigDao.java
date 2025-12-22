package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

/// DAO implementation for companion object [ApplicationConfigCo].
///
@EntityType(ApplicationConfig.class)
public class ApplicationConfigDao extends CommonEntityDao<ApplicationConfig> implements ApplicationConfigCo {

}
