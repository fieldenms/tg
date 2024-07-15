package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ITgEntityWithComplexSummaries}.
 * 
 * @author Developers
 *
 */
@EntityType(TgEntityWithComplexSummaries.class)
public class TgEntityWithComplexSummariesDao extends CommonEntityDao<TgEntityWithComplexSummaries> implements ITgEntityWithComplexSummaries {

    @Inject
    public TgEntityWithComplexSummariesDao(final IFilter filter) {
        super(filter);
    }

}