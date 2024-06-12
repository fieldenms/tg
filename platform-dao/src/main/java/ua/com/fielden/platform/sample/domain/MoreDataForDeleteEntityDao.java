package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link MoreDataForDeleteEntityCo}.
 *
 * @author TG Team
 */
@EntityType(MoreDataForDeleteEntity.class)
public class MoreDataForDeleteEntityDao extends CommonEntityDao<MoreDataForDeleteEntity> implements MoreDataForDeleteEntityCo {

}
