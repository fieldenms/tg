package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgMovement}.
 * 
 * @author Developers
 * 
 */
@EntityType(TgMovement.class)
public class TgMovementDao extends CommonEntityDao<TgMovement> implements ITgMovement {
    @Inject
    public TgMovementDao(final IFilter filter) {
        super(filter);
    }

}