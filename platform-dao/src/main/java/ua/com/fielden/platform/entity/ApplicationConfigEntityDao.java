package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

/// DAO implementation for companion object [ApplicationConfigEntityCo].
///
@EntityType(ApplicationConfigEntity.class)
public class ApplicationConfigEntityDao extends CommonEntityDao<ApplicationConfigEntity> implements ApplicationConfigEntityCo {

}
