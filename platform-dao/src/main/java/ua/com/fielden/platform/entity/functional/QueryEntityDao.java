package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.functional.centre.IQueryEntity;
import ua.com.fielden.platform.entity.functional.centre.QueryEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Map;

import ua.com.fielden.platform.dao.annotations.SessionRequired;

import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IQueryEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(QueryEntity.class)
public class QueryEntityDao extends CommonEntityDao<QueryEntity> implements IQueryEntity {
    @Inject
    public QueryEntityDao(final IFilter filter) {
        super(filter);
    }

}