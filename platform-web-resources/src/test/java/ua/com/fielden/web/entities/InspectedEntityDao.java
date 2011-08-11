package ua.com.fielden.web.entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Test DAO for {@link InspectedEntity} class
 *
 * @author 01es
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
    @SessionRequired
    public void delete(final IQueryOrderedModel<InspectedEntity> model) {
        defaultDelete(model);
    }
}
