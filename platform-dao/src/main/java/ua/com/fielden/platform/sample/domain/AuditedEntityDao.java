package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(AuditedEntity.class)
public class AuditedEntityDao extends CommonEntityDao<AuditedEntity> implements AuditedEntityCo {

}
