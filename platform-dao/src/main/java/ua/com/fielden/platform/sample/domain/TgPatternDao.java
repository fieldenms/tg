package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Map;

/**
 * DAO implementation for companion object {@link TgPatternCo}.
 */
@EntityType(TgPattern.class)
public class TgPatternDao extends CommonEntityDao<TgPattern> implements TgPatternCo {

    @Override
    @SessionRequired
    public void delete(final TgPattern entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgPattern> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

}
