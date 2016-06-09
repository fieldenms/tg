package ua.com.fielden.platform.test.entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.entities.CompositeEntityKey;
import ua.com.fielden.platform.test.entities.ICompositeEntityKey;

@EntityType(CompositeEntityKey.class)
public class CompositeEntityKeyDao extends CommonEntityDao<CompositeEntityKey> implements ICompositeEntityKey {

    @Inject
    protected CompositeEntityKeyDao(IFilter filter) {
        super(filter);
    }

}
