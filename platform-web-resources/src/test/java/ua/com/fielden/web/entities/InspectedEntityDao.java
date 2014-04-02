package ua.com.fielden.web.entities;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Test DAO for {@link InspectedEntity} class
 * 
 * @author TG Team
 * 
 */
@EntityType(InspectedEntity.class)
public class InspectedEntityDao extends CommonEntityDao<InspectedEntity> implements IInspectedEntityDao {

    @Inject
    protected InspectedEntityDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final InspectedEntity entity) {
        defaultDelete(entity);
    }

    @Override
    public void delete(final EntityResultQueryModel<InspectedEntity> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
}
