package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgUnionCommonTypeCo}.
 *
 * @author TG Team
 */
@EntityType(TgUnionCommonType.class)
public class TgUnionCommonTypeDao extends CommonEntityDao<TgUnionCommonType> implements TgUnionCommonTypeCo {

}
