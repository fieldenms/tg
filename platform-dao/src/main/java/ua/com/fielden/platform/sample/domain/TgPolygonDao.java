package ua.com.fielden.platform.sample.domain;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * DAO implementation for companion object {@link ITgPolygon}.
 *
 * @author Developers
 *
 */
@EntityType(TgPolygon.class)
public class TgPolygonDao extends CommonEntityDao<TgPolygon> implements ITgPolygon {

    @Inject
    public TgPolygonDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final TgPolygon entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgPolygon> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
}