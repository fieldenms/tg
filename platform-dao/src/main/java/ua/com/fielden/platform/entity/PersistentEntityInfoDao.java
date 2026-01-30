package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

/// DAO implementation for companion object [PersistentEntityInfoCo].
///
@EntityType(PersistentEntityInfo.class)
public class PersistentEntityInfoDao extends CommonEntityDao<PersistentEntityInfo> implements PersistentEntityInfoCo {

}
