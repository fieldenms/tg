package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
public class TgGeneratedEntityForTrippleDecAnalysisInsertionPointDao extends CommonEntityDao<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> implements IEntityDao<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> {

    @Inject
    protected TgGeneratedEntityForTrippleDecAnalysisInsertionPointDao(final IFilter filter) {
        super(filter);
    }

}
