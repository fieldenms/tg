package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.FWorkOrder;
import ua.com.fielden.platform.example.entities.IFWorkOrderDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(FWorkOrder.class)
public class FWorkOrderDao extends CommonEntityDao<FWorkOrder> implements IFWorkOrderDao {

    @Inject
    protected FWorkOrderDao(final IFilter filter) {
	super(filter);
    }

}
