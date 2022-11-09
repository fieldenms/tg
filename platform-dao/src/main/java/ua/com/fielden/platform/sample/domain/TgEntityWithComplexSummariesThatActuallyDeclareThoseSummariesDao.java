package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries.class)
public class TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesDao extends CommonEntityDao<TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries> implements TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesCo {

    @Inject
    public TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesDao(final IFilter filter) {
        super(filter);
    }

}