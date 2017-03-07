package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;

/** 
 * DAO implementation for companion object {@link ITgCoordinate} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(TgCoordinate.class)
public class TgCoordinateDao extends CommonEntityDao<TgCoordinate> implements ITgCoordinate {

    @Inject
    public TgCoordinateDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public IPage<TgCoordinate> findDetails(final TgPolygon masterEntity, final fetch<TgCoordinate> fetch, final int pageCapacity) {
        final EntityResultQueryModel<TgCoordinate> selectModel = select(TgCoordinate.class).where().prop("polygon").eq().val(masterEntity).model();
        return firstPage(from(selectModel).with(orderBy().prop("order").asc().model()).with(fetch).model(), pageCapacity);
    }
    
    @Override
    public TgCoordinate saveDetails(final TgPolygon masterEntity, final TgCoordinate detailEntity) {
        return save(detailEntity);
    }
    
    @Override
    public void deleteDetails(final TgPolygon masterEntity, final TgCoordinate detailEntity) {
        delete(detailEntity);
    }
    
    @Override
    @SessionRequired
    public void delete(final TgCoordinate entity) {
        defaultDelete(entity);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgCoordinate> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
    
}