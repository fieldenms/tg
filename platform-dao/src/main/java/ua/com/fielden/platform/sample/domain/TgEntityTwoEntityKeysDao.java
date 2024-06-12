package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityTwoEntityKeys}.
 *
 * @author TG Team
 */
@EntityType(TgEntityTwoEntityKeys.class)
public class TgEntityTwoEntityKeysDao extends CommonEntityDao<TgEntityTwoEntityKeys> implements ITgEntityTwoEntityKeys {

}
